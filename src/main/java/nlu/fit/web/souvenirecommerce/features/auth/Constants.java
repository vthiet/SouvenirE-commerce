package nlu.fit.web.souvenirecommerce.features.auth;

import nlu.fit.web.souvenirecommerce.common.utils.ApplicationLoader;

public class Constants {
    public static final String GOOGLE_CLIENT_ID = ApplicationLoader.get("CLIENT_ID") + ".apps.googleusercontent.com";
    public static final String GOOGLE_CLIENT_SECRET = ApplicationLoader.get("CLIENT_SECRET");
    public static final String GOOGLE_REDIRECT_URI = "http://localhost:8080/login-google";
    public static final String GOOGLE_LINK_GET_TOKEN = "https://oauth2.googleapis.com/token";
    public static final String GOOGLE_LINK_GET_USER_INFO = "https://www.googleapis.com/oauth2/v3/userinfo";

    public static final String GITHUB_CLIENT_ID = ApplicationLoader.get("GITHUB_CLIENT_ID");
    public static final String GITHUB_CLIENT_SECRET = ApplicationLoader.get("GITHUB_CECRET");
    public static final String GITHUB_REDIRECT_URI = "http://localhost:8080/login-github";
    public static final String GITHUB_LINK_GET_TOKEN = "https://github.com/login/oauth/access_token";
    public static final String GITHUB_LINK_GET_USER_INFO = "https://api.github.com/user";
    public static final String GITHUB_LINK_GET_EMAILS = "https://api.github.com/user/emails";
}