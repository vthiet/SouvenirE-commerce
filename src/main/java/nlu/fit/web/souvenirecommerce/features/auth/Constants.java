package nlu.fit.web.souvenirecommerce.features.auth;

import nlu.fit.web.souvenirecommerce.common.utils.ApplicationLoader;

public class Constants {
    private static final String REDIRECT_URL = ApplicationLoader.get("REDIRECT_URI");

    // Google OAuth
    public static final String GOOGLE_CLIENT_ID = ApplicationLoader.get("CLIENT_ID") + ".apps.googleusercontent.com";
    public static final String GOOGLE_CLIENT_SECRET = ApplicationLoader.get("CLIENT_SECRET");
    public static final String GOOGLE_REDIRECT_URI = REDIRECT_URL + "/login-google";
    public static final String GOOGLE_LINK_GET_TOKEN = "https://oauth2.googleapis.com/token";
    public static final String GOOGLE_LINK_GET_USER_INFO = "https://www.googleapis.com/oauth2/v3/userinfo";

    // Github OAuth
    public static final String GITHUB_CLIENT_ID = ApplicationLoader.get("GITHUB_CLIENT_ID");
    public static final String GITHUB_CLIENT_SECRET = ApplicationLoader.get("GITHUB_SECRET");
    public static final String GITHUB_REDIRECT_URI = REDIRECT_URL + "/login-github";
    public static final String GITHUB_LINK_GET_TOKEN = "https://github.com/login/oauth/access_token";
    public static final String GITHUB_LINK_GET_USER_INFO = "https://api.github.com/user";
    public static final String GITHUB_LINK_GET_EMAILS = "https://api.github.com/user/emails";

    // Facebook OAuth
    public static final String FACEBOOK_CLIENT_ID = ApplicationLoader.get("FB_CLIENT_ID");
    public static final String FACEBOOK_CLIENT_SECRET = ApplicationLoader.get("FB_SECRET");
    public static final String FACEBOOK_REDIRECT_URI = REDIRECT_URL + "/login-facebook";
}