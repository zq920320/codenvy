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
package com.codenvy.analytics.persistent;

import com.codenvy.analytics.Configurator;
import com.codenvy.organization.client.AccountManager;
import com.codenvy.organization.client.WorkspaceManager;
import com.codenvy.organization.exception.OrganizationServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/** @author Anatoliy Bazko */
@Singleton
public class OrganizationClient {

    private static final Logger LOG                                 = LoggerFactory.getLogger(OrganizationClient.class);
    private static final String ORGANIZATION_APPLICATION_SERVER_URL = "organization.application.server.url";

    private final AccountManager   accountManager;
    private final WorkspaceManager workspaceManager;

    @Inject
    public OrganizationClient(Configurator configurator) throws OrganizationServiceException {
        System.setProperty(ORGANIZATION_APPLICATION_SERVER_URL,
                           configurator.getString(ORGANIZATION_APPLICATION_SERVER_URL));

        accountManager = new AccountManager();
        workspaceManager = new WorkspaceManager();
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public WorkspaceManager getWorkspaceManager() {
        return workspaceManager;
    }
}
