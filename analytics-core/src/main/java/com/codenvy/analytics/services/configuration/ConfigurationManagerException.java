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
package com.codenvy.analytics.services.configuration;

import java.io.IOException;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class ConfigurationManagerException extends IOException {

    /**
     * Constructor for ConfigurationManagerException
     *
     * @param string
     *         message
     * @param e
     *         the cause of exception
     */
    public ConfigurationManagerException(String string, Exception e) {
        super(string, e);
    }

    /**
     * Constructor for ConfigurationManagerException
     *
     * @param string
     *         message
     */
    public ConfigurationManagerException(String string) {
        super(string);
    }

}
