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
package com.codenvy.api.license.server;

import com.codenvy.api.license.server.dao.CodenvyLicenseActionDao;
import com.codenvy.api.license.server.jpa.JpaCodenvyLicenseActionDao;
import com.codenvy.api.permission.server.PermissionChecker;
import com.codenvy.auth.sso.client.TokenHandler;
import com.google.inject.AbstractModule;

import com.google.inject.name.Names;
import org.eclipse.che.inject.DynaModule;

/**
 * @author Alexander Andrienko
 * @author Dmytro Nochevnov
 */
public class LicenseModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(LicenseService.class);
        bind(LicenseServicePermissionsFilter.class);
        bind(CodenvyLicenseActionDao.class).to(JpaCodenvyLicenseActionDao.class);
    }
}
