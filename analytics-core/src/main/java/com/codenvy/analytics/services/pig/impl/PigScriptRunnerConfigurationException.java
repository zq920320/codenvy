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
package com.codenvy.analytics.services.pig.impl;

import java.io.IOException;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class PigScriptRunnerConfigurationException extends IOException {

    /**
     * Constructor for PigScriptRunnerConfigurationException
     *
     * @param string
     *         message
     * @param e
     *         the cause of exception
     */
    public PigScriptRunnerConfigurationException(String string, Exception e) {
        super(string, e);
    }

    /**
     * Constructor for PigScriptRunnerConfigurationException
     *
     * @param string
     *         message
     */
    public PigScriptRunnerConfigurationException(String string) {
        super(string);
    }

}
