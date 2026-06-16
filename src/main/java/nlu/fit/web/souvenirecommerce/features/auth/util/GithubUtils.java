package nlu.fit.web.souvenirecommerce.features.auth.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import nlu.fit.web.souvenirecommerce.features.auth.Constants;
import nlu.fit.web.souvenirecommerce.features.auth.dto.GithubEmailPojo;
import nlu.fit.web.souvenirecommerce.features.auth.dto.GithubPojo;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;

import java.io.IOException;

public class GithubUtils {

    /**
     * Dùng code nhận từ GitHub đổi lấy accessToken
     * @param code
     * @return accessToken
     * @throws IOException
     */
    public static String getToken(final String code) throws IOException {
        String response = Request.Post(Constants.GITHUB_LINK_GET_TOKEN)
                .addHeader("Accept", "application/json")
                .bodyForm(Form.form()
                        .add("client_id", Constants.GITHUB_CLIENT_ID)
                        .add("client_secret", Constants.GITHUB_CLIENT_SECRET)
                        .add("code", code)
                        .build())
                .execute().returnContent().asString();
        JsonObject jobj = new Gson().fromJson(response, JsonObject.class);
        return jobj.get("access_token").toString().replace("\"", "");
    }

    /**
     * Dùng accessToken để lấy thông tin chi tiết của User
     * @param accessToken
     * @return GithubPojo
     * @throws IOException
     */
    public static GithubPojo getUserInfo(final String accessToken) throws IOException {
        String response = Request.Get(Constants.GITHUB_LINK_GET_USER_INFO)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("User-Agent", "Souvenir-Ecommerce-App")
                .execute().returnContent().asString();

        return new Gson().fromJson(response, GithubPojo.class);
    }

    /**
     * Dùng accessToken để lấy email chi tiết từ GitHub API
     * @param accessToken
     * @return email hoặc null nếu không có
     * @throws IOException
     */
    public static String getEmail(final String accessToken) throws IOException {
        String response = Request.Get(Constants.GITHUB_LINK_GET_EMAILS)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("User-Agent", "Souvenir-Ecommerce-App")
                .execute().returnContent().asString();

        GithubEmailPojo[] emails = new Gson().fromJson(response, GithubEmailPojo[].class);
        if (emails != null && emails.length > 0) {
            // Ưu tiên email chính (primary)
            for (GithubEmailPojo emailObj : emails) {
                if (emailObj.isPrimary()) {
                    return emailObj.getEmail();
                }
            }
            // Fallback về email đầu tiên
            return emails[0].getEmail();
        }
        return null;
    }
}
