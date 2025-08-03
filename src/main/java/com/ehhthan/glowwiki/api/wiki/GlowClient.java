package com.ehhthan.glowwiki.api.wiki;

import com.ehhthan.glowwiki.api.event.action.EventAction;
import com.ehhthan.glowwiki.api.wiki.response.TokenResponse;
import com.google.gson.Gson;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;

public class GlowClient {
    private static final Gson gson = new Gson();

    private final String scheme, host, folder, pathSegment, username, password;

    private final OkHttpClient client;

    private final String csrfToken;

    public GlowClient(ConfigurationSection section) {
        this(section.getString("scheme"),
            section.getString("host"),
            section.getString("folder"),
            section.getString("path-segment"),
            section.getString("credentials.username"),
            section.getString("credentials.password"));
    }

    public GlowClient(String scheme, String host, String folder, String pathSegment, String username, String password) {
        this.scheme = scheme;
        this.host = host;
        this.folder = folder;
        this.pathSegment = pathSegment;
        this.username = username;
        this.password = password;

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        this.client = new OkHttpClient.Builder()
            .cookieJar(new JavaNetCookieJar(cookieManager))
            .build();

        try {
            login(tokens("login").logintoken);
            csrfToken = tokens("csrf").csrftoken;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void request(List<EventAction.QueryPair> pairs) throws IOException {
        HttpUrl.Builder builder = url();

        for (EventAction.QueryPair pair : pairs) {
            builder.addQueryParameter(pair.key(), pair.value());
        }

        builder
            .addQueryParameter("bot", "")
            .addQueryParameter("assert", "bot")
            .addQueryParameter("format", "json");

        RequestBody body = new FormBody.Builder()
            .add("token", csrfToken)
            .build();

        Request request = new Request.Builder().url(builder.build()).post(body).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error: " + response.code());
            }
        }
    }

    private HttpUrl.Builder url() {
        return new HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment(folder)
            .addPathSegment(pathSegment);
    }

    private TokenResponse.Tokens tokens(String... types) throws IOException {
        // Grab login token by API
        HttpUrl loginTokenUrl = url()
            .addQueryParameter("action", "query")
            .addQueryParameter("meta", "tokens")
            .addQueryParameter("type", String.join("|", types))
            .addQueryParameter("format", "json")
            .build();

        Request request = new Request.Builder().url(loginTokenUrl).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return gson.fromJson(response.body().string(), TokenResponse.class).query.tokens;
            } else {
                throw new IOException("Error: " + response.code());
            }
        }
    }

    private void login(final String loginToken) throws IOException {
        HttpUrl loginPostUrl = url()
            .addQueryParameter("action", "login")
            .addQueryParameter("lgname", username)
            .build();

        RequestBody body = new FormBody.Builder()
            .add("lgpassword", password)
            .add("lgtoken", loginToken)
            .build();

        Request request = new Request.Builder().url(loginPostUrl).post(body).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error: " + response.code());
            }
        }
    }
}
