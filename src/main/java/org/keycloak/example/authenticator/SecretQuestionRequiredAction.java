package org.keycloak.example.authenticator;

import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.authentication.CredentialRegistrator;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.sessions.AuthenticationSessionModel;

public class SecretQuestionRequiredAction implements RequiredActionProvider, CredentialRegistrator {
    private Logger logger = Logger.getLogger(SecretQuestionRequiredAction.class);
    // 创建ID，类似于ProviderFactory，可以让别人找到它
    public static final String PROVIDER_ID = "secret_question_config";
    @Override
    public void evaluateTriggers(RequiredActionContext requiredActionContext) {

    }

    // 对必需操作的初始调用，即打开一个添加信息的页面
    @Override
    public void requiredActionChallenge(RequiredActionContext requiredActionContext) {
        logger.info("打开添加信息页面...");
        Response response = requiredActionContext.form().createForm("secret-question-config.ftl");
        requiredActionContext.challenge(response);
    }

    // 处理添加信息页面的表单输入，这些信息都会被路由到此方法
    @Override
    public void processAction(RequiredActionContext requiredActionContext) {
        logger.info("处理添加信息的表单数据...");
        String answer = requiredActionContext.getHttpRequest().getDecodedFormParameters().getFirst("secret_answer");
        // 通过Session找到Provider（secret-question是ProviderFactory的ID），目的是为了将输入存储，而存储的方法就写在Provider中
        SecretQuestionCredentialProvider sqcp = (SecretQuestionCredentialProvider) requiredActionContext.getSession().getProvider(CredentialProvider.class, "secret-question");
        sqcp.createCredential(requiredActionContext.getRealm(), requiredActionContext.getUser(), SecretQuestionCredentialModel.createSecretQuestion("你姓什么？", answer));
        requiredActionContext.success();
    }

    @Override
    public void close() {

    }

    @Override
    public String getCredentialType(KeycloakSession keycloakSession, AuthenticationSessionModel authenticationSessionModel) {
        return SecretQuestionCredentialModel.TYPE;
    }
}
