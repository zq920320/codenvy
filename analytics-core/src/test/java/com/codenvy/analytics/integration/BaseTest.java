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

package com.codenvy.analytics.integration;

import com.codenvy.analytics.datamodel.DoubleValueData;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.SetValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import org.eclipse.che.api.auth.server.dto.DtoServerImpls;
import org.eclipse.che.api.auth.shared.dto.Token;
import org.eclipse.che.dto.server.DtoFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codenvy.analytics.Utils.toArray;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.eclipse.che.commons.lang.IoUtil.readAndCloseQuietly;
import static org.junit.Assert.assertEquals;

/**
 * @author Anatoliy Bazko
 */
public class BaseTest {
    public static final Pattern SIMPLE_VALUE_PATTERN = Pattern.compile(".*\"value\"\\s*:\\s*\"([^\"]*)\"");
    public static final Pattern LIST_VALUE_PATTERN   = Pattern.compile(".*\"value\"\\s*:\\s*\"\\[(.*)\\]\"");

    public static final String ADMIN_USER   = "prodadmin";
    public static final String ADMIN_PWD    = "CodenvyAdmin";
    public static final String AUTH_REALM   = "sysldap";
    public static final String API_ENDPOINT = "http://t2.codenvy-dev.com/api";

    private final HTTPTransport transport;
    private final String        accessToken;

    private Map<Class<? extends ValueData>, Pattern> PATTERNS = new HashMap<Class<? extends ValueData>, Pattern>() {{
        put(LongValueData.class, SIMPLE_VALUE_PATTERN);
        put(DoubleValueData.class, SIMPLE_VALUE_PATTERN);
        put(SetValueData.class, SIMPLE_VALUE_PATTERN);
        put(ListValueData.class, LIST_VALUE_PATTERN);
    }};

    public BaseTest() {
        this.transport = new HTTPTransport(API_ENDPOINT);
        try {
            this.accessToken = auth();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected Map<String, Map<String, ValueData>> listToMap(ListValueData valueData, String key) {
        Map<String, Map<String, ValueData>> result = new LinkedHashMap<>();

        for (ValueData item : valueData.getAll()) {
            MapValueData row = (MapValueData)item;
            result.put(row.getAll().get(key).getAsString(), row.getAll());
        }

        return result;
    }

    protected ValueData getValue(MetricType metricType, Context context) throws IOException {
        Metric metric = MetricFactory.getMetric(metricType);

        StringBuilder relPath = new StringBuilder("/analytics/metric/");
        relPath.append(metric.getName());

        for (Map.Entry<String, String> entry : context.getAllAsString().entrySet()) {
            if (relPath.indexOf("?") == -1) {
                relPath.append("?");
            } else {
                relPath.append("&");
            }

            relPath.append(entry.getKey());
            relPath.append("=");
            relPath.append(entry.getValue());
        }

        String json = transport.request("GET", relPath.toString(), null, accessToken);

        return parseResponse(metric, json);
    }

    protected ValueData getValue(MetricType metricType) throws IOException {
        Metric metric = MetricFactory.getMetric(metricType);
        String json = transport.request("GET", "/analytics/metric/" + metric.getName(), null, accessToken);

        return parseResponse(metric, json);
    }

    private ValueData parseResponse(Metric metric, String json) {
        String value;

        Matcher matcher = PATTERNS.get(metric.getValueDataClass()).matcher(json);
        if (matcher.find()) {
            value = matcher.group(1);
        } else {
            throw new IllegalArgumentException("Can't parse value from response: " + json);
        }


        if (metric.getValueDataClass() == LongValueData.class) {
            return LongValueData.valueOf(Integer.parseInt(value));

        } else if (metric.getValueDataClass() == DoubleValueData.class) {
            return DoubleValueData.valueOf(Double.parseDouble(value));

        } else if (metric.getValueDataClass() == SetValueData.class) {
            List<ValueData> l = FluentIterable.from(asList(toArray(value))).transform(new Function<String, ValueData>() {
                @Override
                public ValueData apply(String s) {
                    return StringValueData.valueOf(s);
                }
            }).toList();

            return new SetValueData(l);

        } else if (metric.getValueDataClass() == ListValueData.class) {
            String[] items = value.replace("\\", "").split(", ");
            List<ValueData> l = new ArrayList<>(items.length);

            for (String item : items) {
                Map<String, ValueData> m = new HashMap<>(item.length());
                for (String entry : item.substring(1, item.length() - 1).split(",")) {
                    String[] s = entry.split(":");
                    m.put(s[0].replace("\"", ""), StringValueData.valueOf(s[1].replace("\"", "")));
                }

                l.add(MapValueData.valueOf(m));
            }

            return ListValueData.valueOf(l);
        }

        throw new IllegalArgumentException("Unsupported type " + metric.getValueDataClass());
    }

    protected String auth() throws IOException {
        DtoServerImpls.CredentialsImpl credentials = new DtoServerImpls.CredentialsImpl();
        credentials.setUsername(ADMIN_USER);
        credentials.setPassword(ADMIN_PWD);
        credentials.setRealm(AUTH_REALM);

        String json = transport.request("POST", "/auth/login", credentials, null);
        Token token = DtoFactory.getInstance().createDtoFromJson(json, Token.class);
        return token.getValue();
    }

    protected String getUserNameById(String userId) throws IOException {
        Context.Builder context = new Context.Builder();
        context.put(MetricFilter._ID, userId);

        ValueData userProfile = getValue(MetricType.USERS_PROFILES_LIST, context.build());
        Map<String, Map<String, ValueData>> m = listToMap((ListValueData)userProfile, "aliases");

        assertEquals(m.size(), 1);

        return m.keySet().iterator().next();
    }

    protected String getWsNameById(String wsId) throws IOException {
        Context.Builder context = new Context.Builder();
        context.put(MetricFilter._ID, wsId);

        ValueData wsProfile = getValue(MetricType.WORKSPACES_PROFILES_LIST, context.build());
        Map<String, Map<String, ValueData>> m = listToMap((ListValueData)wsProfile, "ws_name");

        assertEquals(m.size(), 1);

        return m.keySet().iterator().next();
    }

    private class HTTPTransport {
        private final String apiEndpoint;

        public HTTPTransport(String apiEndpoint) {
            this.apiEndpoint = apiEndpoint;
        }

        public String request(String method,
                              String relPath,
                              @Nullable Object body,
                              @Nullable String token) throws IOException {
            return doRequest(relPath, method, body, token);
        }

        private String doRequest(String relPath,
                                 String method,
                                 @Nullable Object body,
                                 @Nullable String token) throws IOException {
            final String requestUrl = apiEndpoint + relPath;

            HttpURLConnection conn = null;
            try {
                conn = openConnection(requestUrl, token);
                doRequest(conn, method, body);
                return readAndCloseQuietly(conn.getInputStream());
            } catch (SocketTimeoutException e) { // catch exception and throw a new one with proper message
                URL url = new URL(requestUrl);
                throw new IOException(format("Can't establish connection with %s://%s", url.getProtocol(), url.getHost()));
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        private void doRequest(HttpURLConnection conn,
                               String method,
                               @Nullable Object body) throws IOException {
            conn.setConnectTimeout(30 * 1000);
            conn.setRequestMethod(method);
            if (body != null) {
                conn.addRequestProperty("content-type", "application/json");
                conn.setDoOutput(true);
                try (OutputStream output = conn.getOutputStream()) {
                    output.write(DtoFactory.getInstance().toJson(body).getBytes());
                }
            }
            final int responseCode = conn.getResponseCode();

            if ((responseCode / 100) != 2) {
                InputStream in = conn.getErrorStream();
                if (in == null) {
                    in = conn.getInputStream();
                }

                throw new IOException("Can't perform request, response from the server: " + readAndCloseQuietly(in));
            }

            final String contentType = conn.getContentType();
            if (contentType != null && !contentType.equalsIgnoreCase("application/json")) {
                throw new IOException("Unsupported type of response from remote server.");
            }
        }

        protected HttpURLConnection openConnection(String path, @Nullable String accessToken) throws IOException {
            HttpURLConnection connection = (HttpURLConnection)new URL(path).openConnection();

            if (accessToken != null) {
                String accessTokenCookie = format("session-access-key=%s;", accessToken);
                connection.addRequestProperty("Cookie", accessTokenCookie);
            }

            return connection;
        }
    }
}
