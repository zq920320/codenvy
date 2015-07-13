/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.docker;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.inject.ConfigurationProperties;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Collects auth configurations for private docker registries. Credential might be configured in .properties files, see details {@link
 * org.eclipse.che.inject.CodenvyBootstrap}. Credentials configured as (key=value) pairs. Key is string that starts with prefix
 * {@code docker.registry.auth.} followed by host name and port (optional) of docker registry server, e.g. {@code
 * docker.registry.auth.localhost:5000}. Value is comma separated pair of username and password, e.g.:
 * <pre>{@code
 * docker.registry.auth.localhost:5000=user:secret
 * }</pre>
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class AuthConfigs {
    private static final String CONFIGURATION_PREFIX         = "docker.registry.auth.";
    private static final String CONFIGURATION_PREFIX_PATTERN = "docker\\.registry\\.auth\\..+";
    private static final String INDEX_SERVER_NAME            = "docker.io";

    private Map<String, String> encodedAuthConfigs;

    @Inject
    AuthConfigs(ConfigurationProperties configurationProperties) {
        final Set<AuthConfig> configs = new HashSet<>();
        for (Map.Entry<String, String> e : configurationProperties.getProperties(CONFIGURATION_PREFIX_PATTERN).entrySet()) {
            final String key = e.getKey();
            final String serverAddress = key.replaceFirst(CONFIGURATION_PREFIX, "");
            final String value = e.getValue();
            String username;
            String password = "";
            final int i = value.indexOf(':');
            final int length = value.length();
            if (i < 0) {
                username = value;
            } else if (i == length) {
                username = value.substring(0, i);
            } else {
                username = value.substring(0, i);
                password = value.substring(i + 1, length);
            }
            configs.add(new AuthConfig(serverAddress, username, password));
        }

        encodedAuthConfigs = new HashMap<>(configs.size());

        for (AuthConfig authConfig : configs) {
            final AuthConfig copyAuthConfig = new AuthConfig(authConfig);
            copyAuthConfig.setServeraddress(null);
            encodedAuthConfigs.put(authConfig.getServeraddress(), Base64.encodeBase64String(JsonHelper.toJson(copyAuthConfig).getBytes()));
        }
    }

    public AuthConfigs(Set<AuthConfig> authConfigs) {
        for (AuthConfig authConfig : authConfigs) {
            final AuthConfig copyAuthConfig = new AuthConfig(authConfig);
            copyAuthConfig.setServeraddress(null);
            encodedAuthConfigs.put(authConfig.getServeraddress(), Base64.encodeBase64String(JsonHelper.toJson(copyAuthConfig).getBytes()));
        }
    }

    public String getAuthHeader(String repositoryName) {
        final String authHeader = encodedAuthConfigs.get(parseIndexName(repositoryName));
        return authHeader == null ? "null" : authHeader;
    }

    private String parseIndexName(String repositoryName) {
        if (repositoryName.contains("://")) {
            throw new IllegalArgumentException(String.format("Invalid repository name '%s'", repositoryName));
        }
        final int i = repositoryName.indexOf('/');
        if (i <= 0) {
            return INDEX_SERVER_NAME;
        }
        final String prefix = repositoryName.substring(0, i);
        if (prefix.indexOf('.') < 0 && prefix.indexOf(':') < 0 && !"localhost".equals(prefix)) {
            return INDEX_SERVER_NAME;
        }
        return prefix;
    }
}
