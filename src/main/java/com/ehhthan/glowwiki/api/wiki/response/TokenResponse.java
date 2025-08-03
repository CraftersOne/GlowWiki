package com.ehhthan.glowwiki.api.wiki.response;

import org.jetbrains.annotations.Nullable;

public class TokenResponse {
    public Query query;

    public static class Query {
        public Tokens tokens;
    }

    public static class Tokens {
        public @Nullable String createaccounttoken, csrftoken, logintoken;
    }
}
