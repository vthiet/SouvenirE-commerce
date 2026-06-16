package nlu.fit.web.souvenirecommerce.features.auth.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class GithubPojo implements Serializable {
    private String id;
    private String email;
    private String name;
    private String login;
    @SerializedName("avatar_url")
    private String avatarUrl;
}
