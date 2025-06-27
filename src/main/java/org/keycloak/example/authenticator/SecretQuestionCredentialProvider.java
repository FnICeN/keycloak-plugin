package org.keycloak.example.authenticator;

import org.jboss.logging.Logger;
import org.keycloak.common.util.Time;
import org.keycloak.credential.*;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;

public class SecretQuestionCredentialProvider implements CredentialProvider<SecretQuestionCredentialModel>, CredentialInputValidator {
    private static final Logger logger = Logger.getLogger(SecretQuestionCredentialProvider.class);

    protected KeycloakSession session;

    // 存储Session的构造方法
    public SecretQuestionCredentialProvider(KeycloakSession session) {
        this.session = session;
    }

    // 是否已配置此自定义Model，会被上层的认证器检查
    @Override
    public boolean isConfiguredFor(RealmModel realmModel, UserModel userModel, String credentialType) {
        if (!supportsCredentialType(credentialType)) return false;
        // 验证存在于存储区
        return userModel.credentialManager().getStoredCredentialsByTypeStream(credentialType).findAny().isPresent();
    }

    //检查输入是否与存储一致，从credentialInput获取用户输入
    @Override
    public boolean isValid(RealmModel realmModel, UserModel userModel, CredentialInput credentialInput) {
        if (!(credentialInput instanceof UserCredentialModel)) {
            logger.debug("CredentialInput需要是UserCredentialModel类型的实例");
            return false;
        }
        if (!(credentialInput.getType().equals(getType()))) return false;
        String challengeResponse = credentialInput.getChallengeResponse();
        if (challengeResponse == null) return false;
        String credentialId = credentialInput.getCredentialId();
        if (credentialId == null || credentialId.isEmpty()) {
            logger.debug("凭据ID为空，可能是其他类型的登录");
            return false;
        }

        // 从存储区获取CredentialModel，然后转化为自定义Model
        CredentialModel cm = userModel.credentialManager().getStoredCredentialById(credentialInput.getCredentialId());
        SecretQuestionCredentialModel sqcm = getCredentialFromModel(cm);
        // 验证自定义Model中的答案是否与输入一致
        return sqcm.getSecretQuestionSecretData().getAnswer().equals(challengeResponse);
    }

    // 获取自定义Model的TYPE
    @Override
    public String getType() {
        return SecretQuestionCredentialModel.TYPE;
    }

    // 存储自定义Model到存储区（即，将封装好的Model对象存储到【凭证】）
    @Override
    public CredentialModel createCredential(RealmModel realmModel, UserModel userModel, SecretQuestionCredentialModel secretQuestionCredentialModel) {
        if (secretQuestionCredentialModel.getCreatedDate() == null)
            secretQuestionCredentialModel.setCreatedDate(Time.currentTimeMillis());
        return userModel.credentialManager().createStoredCredential(secretQuestionCredentialModel);
    }

    // 从存储区删除自定义Model
    @Override
    public boolean deleteCredential(RealmModel realmModel, UserModel userModel, String s) {
        return userModel.credentialManager().removeStoredCredentialById(s);
    }

    // 从已有CredentialModel得到自定义Model
    @Override
    public SecretQuestionCredentialModel getCredentialFromModel(CredentialModel credentialModel) {
        return SecretQuestionCredentialModel.createFromCredentialModel(credentialModel);
    }

    // 设置本Credential的元数据，会被Keycloak展示给用户
    @Override
    public CredentialTypeMetadata getCredentialTypeMetadata(CredentialTypeMetadataContext credentialTypeMetadataContext) {
        return CredentialTypeMetadata.builder()
                .type(getType())
                .category(CredentialTypeMetadata.Category.TWO_FACTOR)
                .displayName(SecretQuestionCredentialProviderFactory.PROVIDER_ID)
                .helpText("secret-question-text")
                .createAction(SecretQuestionAuthenticatorFactory.PROVIDER_ID)
                .removeable(false)
                .build(session);
    }

    @Override
    public boolean supportsCredentialType(String type) {
        return getType().equals(type);
    }
}
