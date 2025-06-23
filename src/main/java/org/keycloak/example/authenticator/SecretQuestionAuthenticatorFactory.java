package org.keycloak.example.authenticator;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

// 工厂类，负责实例化Authenticator并设置此认证器部署的相关信息
public class SecretQuestionAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {
    public static final String PROVIDER_ID = "secret-question-authenticator";
    private static final SecretQuestionAuthenticator SINGLETON = new SecretQuestionAuthenticator();
    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.DISABLED
    };
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    // 在初始化时加入一个显示在UI配置界面的配置选项：cookie时长
    static {
        ProviderConfigProperty pcp = new ProviderConfigProperty();
        pcp.setName("cookie.max.age");
        pcp.setLabel("Cookie最大生存时间");
        pcp.setType(ProviderConfigProperty.STRING_TYPE);
        pcp.setHelpText("Cookie有效期延续时间/秒");
        configProperties.add(pcp);
    }

    // 向上提供create()方法生成认证器实例
    @Override
    public Authenticator create(KeycloakSession keycloakSession) {
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope scope) {

    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

    // 向系统提供该组件的名称
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Secret Question";
    }

    @Override
    public String getReferenceCategory() {
        return "Secret Question";
    }

    // 用户是否可以配置认证器
    @Override
    public boolean isConfigurable() {
        return true;
    }

    // 限制系统中可以为此组件配置的要求
    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    // 是否将调用Authenticator.setRequiredActions()
    // 若设置为必需但未配置认证器，返回false则报错，返回true则调用setRequiredActions()
    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    // 在系统中显示的帮助提示
    @Override
    public String getHelpText() {
        return "密保问题验证器";
    }

    // 在UI界面点击配置小齿轮后将显示哪些配置信息
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }
}
