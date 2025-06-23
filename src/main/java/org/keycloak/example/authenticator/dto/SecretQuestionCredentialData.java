package org.keycloak.example.authenticator.dto;
// 定义答案数据POJO，这里的注解是为了在之后序列化时直接得到{"question" : "<ques>"}的数据
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class SecretQuestionCredentialData {
    private final String question;

    @JsonCreator
    public SecretQuestionCredentialData(@JsonProperty("question")String question) {
        this.question = question;
    }
}
