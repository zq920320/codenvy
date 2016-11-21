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
import com.codenvy.api.license.server.model.impl.CodenvyLicenseActionImpl;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Calendar;

import static java.util.Arrays.asList;

/**
 * @author Anatolii Bazko
 */
@Listeners(TckListener.class)
@Test(suiteName = "CodenvyLicenseDaoTck")
public class CodenvyLicenseDaoTest {
    private CodenvyLicenseActionImpl codenvyLicenseActions[];

    @Inject
    private TckRepository<CodenvyLicenseActionImpl> codenvyLicenseRepository;
    @Inject
    private EventService                            eventService;

    @BeforeMethod
    public void setUp() throws Exception {
        codenvyLicenseActions = new CodenvyLicenseActionImpl[] {new CodenvyLicenseActionImpl(Constants.TYPE.FAIR_SOURCE_LICENSE,
                                                                                             Constants.Action.ACCEPTED,
                                                                                             Calendar.getInstance().getTimeInMillis(),
                                                                                             null,
                                                                                             ImmutableMap.of())};
        codenvyLicenseRepository.createAll(asList(codenvyLicenseActions));
    }

    @Test
    public void testFake() throws Exception {
    }

    @AfterMethod
    public void cleanUp() throws Exception {
        codenvyLicenseRepository.removeAll();
    }
}
