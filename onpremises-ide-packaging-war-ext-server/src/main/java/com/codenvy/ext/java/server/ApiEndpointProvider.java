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
package com.codenvy.ext.java.server;

import com.google.common.base.Strings;

import javax.inject.Provider;

/**
 * Provides value of Che API endpoint URL for usage inside machine to be able to connect to host machine using docker host IP.
 *
 * @author Artem Zatsarynnyi
 */
public class ApiEndpointProvider implements Provider<String> {

    public static final String API_ENDPOINT_URL_VARIABLE = "CHE_API_ENDPOINT";

    @Override
    public String get() {
        return Strings.nullToEmpty(System.getenv(API_ENDPOINT_URL_VARIABLE));
    }
}
