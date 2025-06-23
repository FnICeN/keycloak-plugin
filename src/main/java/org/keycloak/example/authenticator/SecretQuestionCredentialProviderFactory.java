package org.keycloak.example.authenticator;

import org.keycloak.credential.CredentialProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.Provider;

// 工厂类，专用于生产SecretQuestionCredentialProvider实例
public class SecretQuestionCredentialProviderFactory implements CredentialProviderFactory {
    // 这个ID的存在可以使其他类通过Session找到它，从而得到Provider实例
    public static final String PROVIDER_ID = "secret-question";

    // 直接投入Session即可生产实例
    @Override
    public Provider create(KeycloakSession keycloakSession) {
        return new SecretQuestionCredentialProvider(keycloakSession);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
