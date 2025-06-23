package org.keycloak.example.authenticator;

import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class SecretQuestionRequiredActionFactory implements RequiredActionFactory {
    private static final SecretQuestionRequiredAction SINGLETON = new SecretQuestionRequiredAction();
    @Override
    public String getDisplayText() {
        return "Secret Question";
    }

    @Override
    public RequiredActionProvider create(KeycloakSession keycloakSession) {
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

    @Override
    public String getId() {
        return SecretQuestionRequiredAction.PROVIDER_ID;
    }
}
