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

import com.codenvy.api.license.model.Constants;
import com.codenvy.api.license.server.dao.CodenvyLicenseDao;
import com.codenvy.api.license.server.model.impl.CodenvyLicenseActionImpl;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jdbc.jpa.DuplicateKeyException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author Anatolii Bazko
 */
@Singleton
public class JpaCodenvyLicenseDao implements CodenvyLicenseDao {

    @Inject
    protected Provider<EntityManager> managerProvider;

    @Override
    public void store(CodenvyLicenseActionImpl codenvyLicenseAction) throws ServerException, ConflictException {
        requireNonNull(codenvyLicenseAction, "Required non-null codenvy license action");

        try {
            doStore(codenvyLicenseAction);
        } catch (DuplicateKeyException e) {
            throw new ConflictException("Codenvy license action already exists");
        } catch (RuntimeException e) {
            throw new ServerException(e);
        }
    }

    @Override
    public void remove(Constants.Type licenseType, Constants.Action actionType) throws ServerException {
        doRemove(licenseType, actionType);
    }

    @Override
    @Transactional
    public CodenvyLicenseActionImpl getByLicenseAndType(Constants.Type licenseType, Constants.Action actionType) throws ServerException,
                                                                                                                        NotFoundException {
        try {
            return managerProvider.get()
                                  .createNamedQuery("License.getByTypeAndAction", CodenvyLicenseActionImpl.class)
                                  .setParameter("type", licenseType)
                                  .setParameter("action", actionType)
                                  .getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException("Codenvy license action not found");
        } catch (RuntimeException e) {
            throw new ServerException(e);
        }
    }

    @Override
    @Transactional
    public List<CodenvyLicenseActionImpl> getByLicense(Constants.Type licenseType) throws ServerException {
        try {
            return managerProvider.get()
                                  .createNamedQuery("License.getByType", CodenvyLicenseActionImpl.class)
                                  .setParameter("type", licenseType)
                                  .getResultList();
        } catch (RuntimeException e) {
            throw new ServerException(e);
        }
    }

    @Transactional
    protected void doStore(CodenvyLicenseActionImpl codenvyLicenseAction) {
        managerProvider.get().persist(codenvyLicenseAction);
    }

    @Transactional
    protected void doRemove(Constants.Type licenseType, Constants.Action licenseAction) throws ServerException {
        try {
            CodenvyLicenseActionImpl action = getByLicenseAndType(licenseType, licenseAction);
            managerProvider.get().remove(action);
        } catch (NotFoundException e) {
            return;
        }
    }
}
