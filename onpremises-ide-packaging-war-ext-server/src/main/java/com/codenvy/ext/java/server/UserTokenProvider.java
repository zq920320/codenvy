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

import com.google.inject.Provider;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * @author Anton Korneta
 */
public class UserTokenProvider implements Provider<String> {

    public static final String USER_TOKEN = "USER_TOKEN";

    @Override
    public String get() {
        return nullToEmpty(System.getenv(USER_TOKEN));
    }
}
