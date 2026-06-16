package nlu.fit.web.souvenirecommerce.features.auth.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import nlu.fit.web.souvenirecommerce.features.auth.Constants;
import nlu.fit.web.souvenirecommerce.features.auth.dto.FacebookPojo;
import org.apache.http.client.fluent.Request;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FacebookUtils {

    /**
     * Dùng code nhận từ Facebook đổi lấy accessToken
     * @param code
     * @return accessToken
     * @throws IOException
     */
    public static String getToken(final String code) throws IOException {
        String link = "https://graph.facebook.com/v19.0/oauth/access_token?"
                + "client_id=" + Constants.FACEBOOK_CLIENT_ID
                + "&client_secret=" + Constants.FACEBOOK_CLIENT_SECRET
                + "&redirect_uri=" + URLEncoder.encode(Constants.FACEBOOK_REDIRECT_URI, StandardCharsets.UTF_8)
                + "&code=" + code;

        String response = Request.Get(link).execute().returnContent().asString();
        JsonObject jobj = new Gson().fromJson(response, JsonObject.class);
        return jobj.get("access_token").toString().replace("\"", "");
    }

    /**
     * Dùng accessToken để lấy thông tin chi tiết của User
     * @param accessToken
     * @return FacebookPojo
     * @throws IOException
     */
    public static FacebookPojo getUserInfo(final String accessToken) throws IOException {
        String link = "https://graph.facebook.com/me?fields=id,name,email,first_name,last_name,picture.type(large)"
                + "&access_token=" + accessToken;

        String response = Request.Get(link).execute().returnContent().asString();
        return new Gson().fromJson(response, FacebookPojo.class);
    }
}
