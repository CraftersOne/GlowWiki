package com.ehhthan.glowwiki.api.wiki.response;

public class CheckTokenResponse {
    public CheckToken checktoken;

    public static class CheckToken {
        public String result; // valid or invalid
    }
}
