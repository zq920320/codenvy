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


package com.codenvy.analytics.services;

import org.quartz.Job;

import java.util.Map;

/**
 * Extended interface for {@link Job}
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public interface Feature extends Job {

    /**
     * Some feature might be skipped depending on packaging.
     *
     * @return true if feature can be added to scheduler and false if feature has to be ignored
     */
    boolean isAvailable();

    void forceRun(Map<String, String> context);
}
