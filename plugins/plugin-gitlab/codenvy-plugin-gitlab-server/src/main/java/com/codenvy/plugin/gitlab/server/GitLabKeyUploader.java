/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.plugin.gitlab.server;

import com.codenvy.plugin.gitlab.shared.dto.GitLabKey;
import com.google.inject.Inject;

import org.eclipse.che.api.auth.oauth.OAuthTokenProvider;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.git.GitUrlUtils;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.ssh.key.script.SshKeyUploader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Mihail Kuznyetsov
 */
public class GitLabKeyUploader implements SshKeyUploader {


    private static final Logger      LOG                = LoggerFactory.getLogger(GitLabKeyUploader.class);
    private static final     Pattern GITLAB_URL_PATTERN = Pattern.compile(".*gitlab\\.codenvy-stg\\.com.*");
    private OAuthTokenProvider tokenProvider;

    private String host;

    @Inject
    public GitLabKeyUploader(OAuthTokenProvider tokenProvider) {
        this.host = "gitlab.codenvy-stg.com";
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void uploadKey(String publicKey) throws IOException, UnauthorizedException {
            final OAuthToken token = tokenProvider.getToken("gitlab.codenvy-stg.com", EnvironmentContext.getCurrent().getSubject().getUserId());

            if (token == null || token.getToken() == null) {
                LOG.debug("Token not found, user need to authorize to upload key.");
                throw new UnauthorizedException("To upload SSH key you need to authorize.");
            }

            StringBuilder answer = new StringBuilder();
            final String url = String.format("http://" + host + "/api/v3/user/keys?access_token=%s", token.getToken());

            final List<GitLabKey> gitLabUserPublicKeys = getUserPublicKeys(url, answer);
            for (GitLabKey gitLabUserPublicKey : gitLabUserPublicKeys) {
                if (publicKey.startsWith(gitLabUserPublicKey.getKey())) {
                    return;
                }
            }

            final Map<String, String> postParams = new HashMap<>(2);
            postParams.put("title", "IDE SSH Key (" + new SimpleDateFormat().format(new Date()) + ")");
            postParams.put("key", publicKey);

            final String postBody = JsonHelper.toJson(postParams);

            LOG.debug("Upload public key: {}", postBody);

            int responseCode;
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection)new URL(url).openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod(HttpMethod.POST);
                conn.setRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
                conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                conn.setRequestProperty(HttpHeaders.CONTENT_LENGTH, String.valueOf(postBody.length()));
                conn.setDoOutput(true);
                try (OutputStream out = conn.getOutputStream()) {
                    out.write(postBody.getBytes("UTF-8"));
                }
                responseCode = conn.getResponseCode();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            LOG.debug("Upload key response code: {}", responseCode);

            if (responseCode != HttpURLConnection.HTTP_CREATED) {
                throw new IOException(String.format("%d: Failed to upload public key to http://gitlab.com/", responseCode));
            }
    }

    @Override
    public boolean match(String url) {
        return GitUrlUtils.isSSH(url) && GITLAB_URL_PATTERN.matcher(url).matches();
    }

    private List<GitLabKey> getUserPublicKeys(String url, StringBuilder answer) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection)new URL(url).openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod(HttpMethod.GET);
            conn.setRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        answer.append(line).append('\n');
                    }
                }
                if (conn.getHeaderFields().containsKey("Link")) {
                    String strForParsing = conn.getHeaderFields().get("Link").get(0);
                    int indexNext = strForParsing.indexOf("rel=\"next\"");

                    if (indexNext != -1) {
                        String nextSubStr = strForParsing.substring(0, indexNext);
                        String nextPageLink = nextSubStr.substring(nextSubStr.indexOf("<") + 1, nextSubStr.indexOf(">"));

                        getUserPublicKeys(nextPageLink, answer);
                    }
                    int indexToReplace;
                    while ((indexToReplace = answer.indexOf("]\n[")) != -1) {
                        answer.replace(indexToReplace, indexToReplace + 3, ",");
                    }
                }
                return DtoFactory.getInstance().createListDtoFromJson(answer.toString(), GitLabKey.class);
            }
            return Collections.emptyList();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
