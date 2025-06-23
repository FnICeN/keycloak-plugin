package org.keycloak.example.authenticator;

import org.keycloak.example.authenticator.dto.SecretQuestionCredentialData;
import org.keycloak.example.authenticator.dto.SecretQuestionSecretData;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;

public class SecretQuestionCredentialModel extends CredentialModel {
    public static final String TYPE = "SECRET_QUESTION";

    private final SecretQuestionCredentialData credentialData;  // 问题POJO
    private final SecretQuestionSecretData secretData;  // 答案POJO

    public SecretQuestionCredentialData getSecretQuestionCredentialData() {
        return credentialData;
    }

    public SecretQuestionSecretData getSecretQuestionSecretData() {
        return secretData;
    }

    // 构造方法1，从问题和答案的POJO构造自定义Model，保存这两个POJO到Model对象中
    private SecretQuestionCredentialModel(SecretQuestionCredentialData credentialData, SecretQuestionSecretData secretData) {
        this.credentialData = credentialData;
        this.secretData = secretData;
    }

    // 构造方法2，当场创建POJO并保存到Model对象
    private SecretQuestionCredentialModel(String question, String answer) {
        this.credentialData = new SecretQuestionCredentialData(question);
        this.secretData = new SecretQuestionSecretData(answer);
    }

    // 根据已有的CredentialModel基类获取【保存了两个POJO以及原基类所有凭证属性】的自定义Model对象
    // 写这个静态类是因为之后的Provider中需要重写相应方法，而那个方法就需要根据传入的CredentialModel得到具体自定义Model
    public static SecretQuestionCredentialModel createFromCredentialModel(CredentialModel credentialModel) {
        try {
            // 分别将从credentialModel读取到的、JSON形式的问题和答案转化为自定义出的两种POJO对象
            SecretQuestionCredentialData credentialData = JsonSerialization.readValue(credentialModel.getCredentialData(), SecretQuestionCredentialData.class);
            SecretQuestionSecretData secretData = JsonSerialization.readValue(credentialModel.getSecretData(), SecretQuestionSecretData.class);
            // 使用两种POJO构造自定义Model，并填充各信息（这次以JSON序列化的形式将问题和答案再次保存）
            SecretQuestionCredentialModel secretQuestionCredentialModel = new SecretQuestionCredentialModel(credentialData, secretData);
            secretQuestionCredentialModel.setUserLabel(credentialModel.getUserLabel());
            secretQuestionCredentialModel.setCreatedDate(credentialModel.getCreatedDate());
            secretQuestionCredentialModel.setType(credentialModel.getType());
            secretQuestionCredentialModel.setId(credentialModel.getId());
            secretQuestionCredentialModel.setCredentialData(credentialModel.getCredentialData());
            secretQuestionCredentialModel.setSecretData(credentialModel.getSecretData());
            return secretQuestionCredentialModel;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // 从仅有问题和答案的情况下获得自定义Model，相较于从CredentialModel获得，缺少Id和UserLabel属性
    // 写这个方法的目的是，在之后用户添加新问答时当场创建一个可以存储的自定义Model，作为Provider中存储方法的第三个参数
    public static SecretQuestionCredentialModel createSecretQuestion(String question, String answer) {
        SecretQuestionCredentialModel sqcm = new SecretQuestionCredentialModel(question, answer);
        // 此时的sqcm已经保存着TYPE和两个POJO
        sqcm.fillFields();
        return sqcm;
    }

    // 工具方法，为自己装填属性
    private void fillFields() {
        try {
            setCredentialData(JsonSerialization.writeValueAsString(credentialData));
            setSecretData(JsonSerialization.writeValueAsString(secretData));
            setType(this.TYPE);
            setCreatedDate(Time.currentTimeMillis());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
