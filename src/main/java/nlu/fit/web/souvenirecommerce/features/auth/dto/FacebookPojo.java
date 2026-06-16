package nlu.fit.web.souvenirecommerce.features.auth.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class FacebookPojo implements Serializable {
    private String id;
    private String email;
    private String name;
    @SerializedName("first_name")
    private String firstName;
    @SerializedName("last_name")
    private String lastName;
    private Picture picture;

    @Setter
    @Getter
    public static class Picture implements Serializable {
        private PictureData data;
    }

    @Setter
    @Getter
    public static class PictureData implements Serializable {
        private String url;
    }

    public String getAvatarUrl() {
        return picture != null && picture.getData() != null ? picture.getData().getUrl() : null;
    }
}
