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
package com.codenvy.service.http;

/**
 * Holder for IDE version ThreadLocal
 */
public class IdeVersionHolder {

    private static final ThreadLocal<Boolean> isIDE2 = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    public static boolean get() {
        return isIDE2.get().booleanValue();
    }

    public static void set(boolean value) {
         isIDE2.set(Boolean.valueOf(value));
    }

    public static void remove() {
        isIDE2.remove();
    }
}
