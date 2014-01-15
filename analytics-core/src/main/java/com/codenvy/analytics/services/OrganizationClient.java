/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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

import com.codenvy.analytics.Configurator;
import com.codenvy.organization.client.AccountManager;
import com.codenvy.organization.exception.OrganizationServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Anatoliy Bazko */
public class OrganizationClient {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationClient.class);

    private static final String ORGANIZATION_APPLICATION_SERVER_URL = "organization.application.server.url";

    private static final AccountManager accountManager;

    static {
        System.setProperty(ORGANIZATION_APPLICATION_SERVER_URL,
                           Configurator.getString(ORGANIZATION_APPLICATION_SERVER_URL));

        try {
            accountManager = new AccountManager();
        } catch (OrganizationServiceException e) {
            LOG.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    public static AccountManager getAccountManager() {
        return accountManager;
    }

}
