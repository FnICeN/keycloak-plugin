package org.keycloak.example.authenticator.dto;
// 定义答案数据POJO，这里的注解是为了在之后序列化时直接得到{"secret" : "<answer>"}的数据
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class SecretQuestionSecretData {
    private final String answer;

    @JsonCreator
    public SecretQuestionSecretData(@JsonProperty("secret")String answer) {
        this.answer = answer;
    }
}
