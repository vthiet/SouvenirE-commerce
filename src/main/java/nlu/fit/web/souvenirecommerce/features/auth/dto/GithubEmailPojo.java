package nlu.fit.web.souvenirecommerce.features.auth.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class GithubEmailPojo implements Serializable {
    private String email;
    private boolean primary;
    private boolean verified;
    private String visibility;
}
