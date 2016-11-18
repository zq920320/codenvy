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
package com.codenvy.api.license.server.jpa;

import com.codenvy.api.license.server.dao.CodenvyLicenseDao;
import com.codenvy.api.license.server.model.impl.CodenvyLicenseActionImpl;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

import org.eclipse.che.api.core.jdbc.jpa.DuplicateKeyException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;

/**
 * @author Anatolii Bazko
 */
@Singleton
public class JpaCodenvyLicenseDao implements CodenvyLicenseDao {

    @Inject
    protected Provider<EntityManager> managerProvider;

    @Override
    @Transactional
    public void store(CodenvyLicenseActionImpl codenvyLicenseAction) {
        try {
            managerProvider.get().persist(codenvyLicenseAction);
        } catch (DuplicateKeyException e) {

        } catch (RuntimeException e) {

        }
    }
}
