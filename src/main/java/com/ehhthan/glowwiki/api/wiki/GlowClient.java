package com.ehhthan.glowwiki.api.wiki;

import com.ehhthan.glowwiki.GlowWiki;
import com.ehhthan.glowwiki.api.wiki.response.CheckTokenResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GlowClient {
    private static final Gson gson = new Gson();

    private final String link;
    private final String scheme, host, folder, pathSegment, username, password;

    private final OkHttpClient client;
    private final Logger logger;

    private String csrfToken;

    public GlowClient(ConfigurationSection section) {
        this(
            section.getString("link"),
            section.getString("scheme"),
            section.getString("host"),
            section.getString("folder"),
            section.getString("path-segment"),
            section.getString("credentials.username"),
            section.getString("credentials.password")
        );
    }

    public GlowClient(String link, String scheme, String host, String folder, String pathSegment, String username, String password) {
        this.link = link;
        this.scheme = scheme;
        this.host = host;
        this.folder = folder;
        this.pathSegment = pathSegment;
        this.username = username;
        this.password = password;

        // mediawiki needs cookies
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        // create client
        this.client = new OkHttpClient.Builder()
            .cookieJar(new JavaNetCookieJar(cookieManager))
            .build();

        this.logger = GlowWiki.getInstance().getLogger();

        refresh();
    }

    public void refresh() {
        try {
            logout();
            login(tokens("login").logintoken);
            csrfToken = tokens("csrf").csrftoken;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String link(String slug) {
        return String.format("%s%s", link, slug);
    }

    private HttpUrl.Builder url() {
        return new HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment(folder)
            .addPathSegment(pathSegment);
    }

    private TokenResponse.Tokens tokens(String... types) throws IOException {
        String response = request(List.of(
            ParamPair.of("action", "query"),
            ParamPair.of("meta", "tokens"),
            ParamPair.of("type", String.join("|", types))
            ));

        return gson.fromJson(response, TokenResponse.class).query.tokens;
    }

    private void login(final String loginToken) throws IOException {
        request(
            List.of(
                ParamPair.of("action", "login"),
                ParamPair.of("lgname", username)
            ),
            List.of(
                ParamPair.of("lgpassword", password),
                ParamPair.of("lgtoken", loginToken)
            )
        );
    }

    private void logout() throws IOException {
        // don't try to logout unless token exists.
        if (csrfToken == null)
            return;

        request(
            List.of(
                ParamPair.of("action", "logout")
            )
        );
    }

    private boolean checkCSRFToken() {
        HttpUrl checkTokenUrl = url()
            .addQueryParameter("action", "checktoken")
            .addQueryParameter("format", "json")
            .addQueryParameter("type", "csrf")
            .build();

        RequestBody body = new FormBody.Builder()
            .add("token", csrfToken)
            .build();

        Request request = new Request.Builder().url(checkTokenUrl).post(body).build();
        try (Response response = client.newCall(request).execute()) {
            return gson.fromJson(response.body().string(), CheckTokenResponse.class)
                .checktoken.result.equalsIgnoreCase("valid");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            return false;
        }
    }

    // bodyless request
    public String botRequest(List<ParamPair> query) throws IOException {
        return botRequest(query, new ArrayList<>());
    }

    public String botRequest(List<ParamPair> query, List<ParamPair> body) throws IOException {
        // refresh token if needed.
        if (!checkCSRFToken()) {
            refresh();
            logger.log(Level.INFO, "Token refreshed.");
        }

        query.add(ParamPair.of("bot", ""));
        query.add(ParamPair.of("assert", "bot"));

        return request(query, body);
    }

    // bodyless request
    private String request(List<ParamPair> query) throws IOException {
        return request(query, new ArrayList<>());
    }

    private String request(List<ParamPair> query, List<ParamPair> body) throws IOException {
        // URL Builder
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment(folder)
            .addPathSegment(pathSegment);

        for (ParamPair pair : query) {
            urlBuilder.addQueryParameter(pair.key(), pair.value());
        }

        urlBuilder
            .addQueryParameter("format", "json");

        // Body Builder
        // no, not that one.
        FormBody.Builder bodyBuilder = new FormBody.Builder();

        for (ParamPair pair : body) {
            bodyBuilder.add(pair.key(), pair.value());
        }

        if (csrfToken != null) {
            bodyBuilder.add("token", csrfToken);
        }

        RequestBody requestBody = bodyBuilder.build();

        // Sends request; returns response.
        Request request = new Request.Builder().url(urlBuilder.build()).post(requestBody).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error: " + response.code());
            }

            return response.body().string();
        }
    }
}
