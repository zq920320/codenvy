/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */

package com.codenvy.analytics.metrics;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class MessageTransformer {

    /**
     * @param rawMessage the raw message from the log
     * @return message in user readable form
     */
    public static List<String> transform(String rawMessage) {
        String dt = extractDateTime(rawMessage);
        String user = extractUser(rawMessage);
        String ws = extractWs(rawMessage);
        String message = extractMessage(rawMessage);

        StringBuilder builder = new StringBuilder();
        builder.append(message);

        if (!ws.isEmpty() && !message.contains("WS#")) {
            builder.append(" WS#");
            builder.append(ws);
            builder.append("#");
        }

        if (!user.isEmpty() && !message.contains("USER#")) {
            builder.append(" USER#");
            builder.append(user);
            builder.append("#");
        }

        List<String> result = new ArrayList<String>(2);
        result.add(dt);
        result.add(builder.toString());

        return result;
    }

    private static String extractMessage(String rawMessage) {
        return rawMessage.replaceFirst(".*\\[.*\\]\\[.*\\]\\[.*\\] - (.*)", "$1");
    }
    private static String extractWs(String rawMessage) {
        return rawMessage.replaceFirst(".*\\[.*\\]\\[(.*)\\]\\[.*\\] - .*", "$1");
    }

    private static String extractUser(String rawMessage) {
        return rawMessage.replaceFirst(".*\\[(.*)\\]\\[.*\\]\\[.*\\] - .*", "$1");
    }

    private static String extractParam(String rawMessage, String param) {
        return rawMessage.replaceFirst(".*" + param + "\\#([^\\#]*)\\#.*", "$1");
    }

    private static String extractDateTime(String rawMessage) {
        return rawMessage.replaceFirst("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3} ([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}).*",
                                       "$1");
    }

}
