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
package com.codenvy.api.license.server.spi.tck;

import com.codenvy.api.license.model.Constants;
import com.codenvy.api.license.server.jpa.JpaCodenvyLicenseDao;
import com.codenvy.api.license.server.model.impl.CodenvyLicenseActionImpl;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatolii Bazko
 */
@Listeners(TckListener.class)
@Test(suiteName = "CodenvyLicenseDaoTck")
public class JpaCodenvyLicenseDaoTest {
    private CodenvyLicenseActionImpl codenvyLicenseActions[];

    @Inject
    private TckRepository<CodenvyLicenseActionImpl> codenvyLicenseRepository;
    @Inject
    private EventService                            eventService;
    @Inject
    private JpaCodenvyLicenseDao                    codenvyLicenseDao;

    @BeforeMethod
    public void setUp() throws Exception {
        codenvyLicenseActions = new CodenvyLicenseActionImpl[] {new CodenvyLicenseActionImpl(Constants.Type.FAIR_SOURCE_LICENSE,
                                                                                             Constants.Action.ACCEPTED,
                                                                                             System.currentTimeMillis(),
                                                                                             null,
                                                                                             ImmutableMap.of("prop1", "value1")),
                                                                new CodenvyLicenseActionImpl(Constants.Type.PRODUCT_LICENSE,
                                                                                             Constants.Action.ACCEPTED,
                                                                                             System.currentTimeMillis(),
                                                                                             "licenseQualifier1",
                                                                                             ImmutableMap.of()),
                                                                new CodenvyLicenseActionImpl(Constants.Type.PRODUCT_LICENSE,
                                                                                             Constants.Action.EXPIRED,
                                                                                             System.currentTimeMillis(),
                                                                                             "licenseQualifier1",
                                                                                             ImmutableMap.of())};
        codenvyLicenseRepository.createAll(asList(codenvyLicenseActions));
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowExceptionIfLicenseActionAlreadyExists() throws Exception {
        codenvyLicenseDao.store(new CodenvyLicenseActionImpl(Constants.Type.FAIR_SOURCE_LICENSE,
                                                             Constants.Action.ACCEPTED,
                                                             System.currentTimeMillis(),
                                                             null,
                                                             ImmutableMap.of()));
    }

    @Test
    public void shouldFindRecordByTypeAndAction() throws Exception {
        CodenvyLicenseActionImpl codenvyLicenseAction =
                codenvyLicenseDao.getByLicenseAndType(Constants.Type.FAIR_SOURCE_LICENSE, Constants.Action.ACCEPTED);

        assertNotNull(codenvyLicenseAction);
        assertNotNull(codenvyLicenseAction.getAttributes().isEmpty());
        assertEquals(codenvyLicenseAction.getAttributes().get("prop1"), "value1");
    }

    @Test
    public void shouldFindRecordByType() throws Exception {
        List<CodenvyLicenseActionImpl> actions = codenvyLicenseDao.getByLicense(Constants.Type.PRODUCT_LICENSE);
        assertEquals(actions.size(), 2);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionIfActionNotFound() throws Exception {
        codenvyLicenseDao.getByLicenseAndType(Constants.Type.FAIR_SOURCE_LICENSE, Constants.Action.EXPIRED);
    }

    @Test
    public void shouldRemoveCodenvyLicenseAction() throws Exception {
        codenvyLicenseDao.remove(Constants.Type.PRODUCT_LICENSE);

        List<CodenvyLicenseActionImpl> actions = codenvyLicenseDao.getByLicense(Constants.Type.PRODUCT_LICENSE);

        assertTrue(actions.isEmpty());
    }

    @AfterMethod
    public void cleanUp() throws Exception {
        codenvyLicenseRepository.removeAll();
    }
}
