package org.keycloak.example.authenticator;

import com.DeviceAuthApi.DeviceAuthConstants;
import com.DeviceAuthApi.DeviceAuthCredentialModel;
import com.DeviceAuthApi.DeviceAuthCredentialProvider;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.CredentialValidator;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.models.*;

import java.net.URI;

// 认证器类，通过对Credential操作完成认证过程
public class SecretQuestionAuthenticator implements Authenticator, CredentialValidator<SecretQuestionCredentialProvider> {
    private Logger logger = Logger.getLogger(SecretQuestionAuthenticator.class);

    // 使得认证器可以找到需要使用的Provider
    @Override
    public SecretQuestionCredentialProvider getCredentialProvider(KeycloakSession keycloakSession) {
        return (SecretQuestionCredentialProvider) keycloakSession.getProvider(CredentialProvider.class, SecretQuestionCredentialProviderFactory.PROVIDER_ID);
    }

    // 进行认证时的操作，并不负责处理表单，只负责呈现页面或继续流程
    @Override
    public void authenticate(AuthenticationFlowContext authenticationFlowContext) {
        logger.info("进入认证，呈现页面...");
//        if (hasCookie(authenticationFlowContext)) {
            // 将上下文标记为success然后返回
//            authenticationFlowContext.success();
//            return;
//        }
        // 没回答过，创建一个页面然后作为挑战呈现给用户
        // 这里的页面是使用已有的模板通过FreeMaker页面构建器构建的，创建后得到JAX-RS Response对象
        Response challenge = authenticationFlowContext.form().createForm("secret-question.ftl");
        // 将此对象传入上下文的challenge()方法，将执行的状态设置为CHALLENGE并将对象发送到浏览器
        authenticationFlowContext.challenge(challenge);
    }

    // 呈现挑战页面、用户输入点击提交后需要做的事情，在这里处理表单数据
    @Override
    public void action(AuthenticationFlowContext authenticationFlowContext) {
        logger.info("进入action方法，准备验证表单数据...");
        // 调用自建的验证方法验证上下文中用户提交的答案是否正确
        boolean validated = validateAnswer(authenticationFlowContext);
        if(!validated) {
            logger.info("答案错误");
            // 答案错误，构建失败页面，加入自定义错误提示，然后呈现Response
            Response challenge = authenticationFlowContext.form().setError("答案错误").createForm("secret-question.ftl");
            // failureChallenge()与challenge()作用相同，但会将失败记录到日志
            authenticationFlowContext.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }
        // 答案正确，记录Cookie然后给上下文标记成功
        // 如果是由DeviceAuth转来，则保存设备信息
        setCookie(authenticationFlowContext);
        if (isRegisterDevice(authenticationFlowContext)) {
            logger.info("隐私问题认证成功，保存设备信息...");
            String cpuid = authenticationFlowContext.getAuthenticationSession().getClientNote("cpuid");
            String visitorId = authenticationFlowContext.getAuthenticationSession().getClientNote("visitorId");
            logger.info("cpuid: " + cpuid);
            logger.info("visitorId: " + visitorId);
            DeviceAuthCredentialProvider dacp = (DeviceAuthCredentialProvider) authenticationFlowContext.getSession().getProvider(CredentialProvider.class, DeviceAuthConstants.credentialProviderFactoryID);
            dacp.createCredential(authenticationFlowContext.getRealm(), authenticationFlowContext.getUser(), DeviceAuthCredentialModel.createDeviceAuth("new", cpuid, visitorId));
            // TODO：确保设备名称不相同，否则会导致系统错误
            logger.info("成功保存新设备信息");
        }
        authenticationFlowContext.success();
    }

    // 设置Cookie内容，用于标记是否已回答过问题
    protected void setCookie(AuthenticationFlowContext context) {
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        int maxCookieAge = 60 * 2;
        if (config != null) {
            // 如果已有寿命配置，则设置为最大期限，之后需要在AuthenticatorFactory中设置参数，以使得寿命配置可以显示在前端控制台
            maxCookieAge = Integer.valueOf(config.getConfig().get("cookie.max.age"));
        }
        URI uri = context.getUriInfo().getBaseUriBuilder().path("realms").path(context.getRealm().getName()).build();
        NewCookie newCookie = new NewCookie.Builder("SECRET_QUESTION_ANSWERED").value("true")
                .path(uri.getRawPath())
                .maxAge(maxCookieAge)
                .secure(false)
                .build();
        context.getSession().getContext().getHttpResponse().setCookieIfAbsent(newCookie);
    }

    // 从上下文检查cookie，是否已经回答过问题，这里的SECRET_QUESTION_ANSWERED字段是自定义的，所以也需要手动设置Cookie内容
    protected boolean hasCookie(AuthenticationFlowContext context) {
        Cookie cookie = context.getHttpRequest().getHttpHeaders().getCookies().get("SECRET_QUESTION_ANSWERED");
        boolean result = cookie != null;
        if (result) {
            logger.info("Cookie存在，跳过认证问题");
        }
        return result;
    }

    // 从上下文检查提交的答案是否正确，调用Provider的isValid()方法
    protected boolean validateAnswer(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        // 得到输入的答案
        String secret = formData.getFirst("secret_answer");
        logger.info("输入的答案：" + secret);
        // 获取一下Id，考虑到用户可能设置多个Credential，需要确定使用何种答案来验证用户
        String credentialId = formData.getFirst("credentialId");
        if (credentialId == null || credentialId.isEmpty()) {
            credentialId = getCredentialProvider(context.getSession()).getDefaultCredential(context.getSession(), context.getRealm(), context.getUser()).getId();
        }
        // 构造能够使Provider.isValid()进行判断的UserCredentialModel实例
        UserCredentialModel input = new UserCredentialModel(credentialId, getType(context.getSession()), secret);
        return getCredentialProvider(context.getSession()).isValid(context.getRealm(), context.getUser(), input);
    }

    protected boolean isRegisterDevice(AuthenticationFlowContext context) {
        String registeringDevice = context.getAuthenticationSession().getClientNote("registeringDevice");
        return "true".equals(registeringDevice);
    }

    // 需要认证的内容是与用户关联的，所以需要返回True
    @Override
    public boolean requiresUser() {
        return true;
    }

    // 查看是否已经配置此认证器，因为下层的Provider已经写好判断逻辑所以直接调用即可
    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        logger.info("查看是否配置认证器...");
        boolean flag = getCredentialProvider(keycloakSession).isConfiguredFor(realmModel, userModel, getType(keycloakSession));
        logger.info("配置认证器结果：" + flag);
        return getCredentialProvider(keycloakSession).isConfiguredFor(realmModel, userModel, getType(keycloakSession));
    }

    // 若未配置认证器但设置为必需，则会调用此方法，这里即为要求用户设置问题和答案。只需要【注册】操作即可（即指向一个RequiredAction实例），操作的实现逻辑在其他地方编写
    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        logger.info("未配置，但必须，所以设置注册RequiredAction");
        userModel.addRequiredAction(SecretQuestionRequiredAction.PROVIDER_ID);
    }

    @Override
    public void close() {

    }
}
