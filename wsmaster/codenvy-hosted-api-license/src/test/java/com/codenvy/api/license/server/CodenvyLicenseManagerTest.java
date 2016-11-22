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

import com.codenvy.api.license.CodenvyLicense;
import com.codenvy.api.license.CodenvyLicenseFactory;
import com.codenvy.api.license.LicenseNotFoundException;
import com.codenvy.api.license.server.dao.CodenvyLicenseDao;
import com.codenvy.api.license.server.model.impl.CodenvyLicenseActionImpl;
import com.codenvy.swarm.client.SwarmDockerConnector;
import com.codenvy.swarm.client.model.DockerNode;
import com.google.common.hash.Hashing;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

import static com.codenvy.api.license.CodenvyLicense.MAX_NUMBER_OF_FREE_SERVERS;
import static com.codenvy.api.license.CodenvyLicense.MAX_NUMBER_OF_FREE_USERS;
import static com.codenvy.api.license.model.Constants.Action.ACCEPTED;
import static com.codenvy.api.license.model.Constants.Action.EXPIRED;
import static com.codenvy.api.license.model.Constants.Type.PRODUCT_LICENSE;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.nio.charset.Charset.defaultCharset;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatoliy Bazko
 * @author Alexander Andrienko
 * @author Dmytro Nochevnov
 */
@Listeners(value = {MockitoTestNGListener.class})
public class CodenvyLicenseManagerTest {

    private static final String LICENSE_TEXT               = "license text";
    private static final String NEW_LICENSE_TEXT           = "new license text";
    private static final String LICENSE_STORAGE_PREFIX_DIR = "licenseStorage-";
    private static final String LICENSE                    = "license";
    private static final String LICENSE_QUALIFIER          = Hashing.md5().hashString(LICENSE_TEXT, defaultCharset()).toString();
    private static final long   USER_NUMBER                = 3;
    private static final int    NODES_NUMBER               = 2;

    @Mock
    private CodenvyLicense        mockCodenvyLicense;
    @Mock
    private CodenvyLicenseFactory licenseFactory;
    @Mock
    private CodenvyLicense        codenvyLicense;
    @Mock
    private CodenvyLicense        newCodenvyLicense;
    @Mock
    private SwarmDockerConnector  swarmDockerConnector;
    @Mock
    private UserManager           userManager;
    @Mock
    private List<DockerNode>      dockerNodes;
    @Mock
    private CodenvyLicenseDao     codenvyLicenseDao;

    private File testDirectory;
    private File licenseFile;

    private CodenvyLicenseManager codenvyLicenseManager;

    @BeforeMethod
    public void setUp() throws IOException, ServerException {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(resource);

        File targetDir = new File(resource.getPath()).getParentFile();
        testDirectory = new File(targetDir, NameGenerator.generate(LICENSE_STORAGE_PREFIX_DIR, 4));
        licenseFile = new File(testDirectory, LICENSE);
        Files.createDirectories(testDirectory.toPath());

        when(licenseFactory.create(LICENSE_TEXT)).thenReturn(codenvyLicense);
        when(licenseFactory.create(NEW_LICENSE_TEXT)).thenReturn(newCodenvyLicense);
        when(codenvyLicense.getLicenseText()).thenReturn(LICENSE_TEXT);
        when(newCodenvyLicense.getLicenseText()).thenReturn(NEW_LICENSE_TEXT);
        when(userManager.getTotalCount()).thenReturn(USER_NUMBER);

        setSizeOfAdditionalNodes(NODES_NUMBER);

        codenvyLicenseManager = spy(new CodenvyLicenseManager(licenseFile.getAbsolutePath(),
                                                              licenseFactory,
                                                              userManager,
                                                              swarmDockerConnector,
                                                              codenvyLicenseDao));
    }

    @AfterMethod
    public void tearDown() throws Exception {
        IoUtil.deleteRecursive(testDirectory);
    }

    /**
     * Use case:
     *  - user adds license
     * Verify:
     *  - license is stored in the file
     *  - license action is stored in the DB
     */
    @Test
    public void shouldStoreLicense() throws Exception {
        ArgumentCaptor<CodenvyLicenseActionImpl> actionCaptor = ArgumentCaptor.forClass(CodenvyLicenseActionImpl.class);
        when(codenvyLicenseDao.getByLicenseAndType(PRODUCT_LICENSE, ACCEPTED))
                .thenThrow(new NotFoundException("Codenvy license action not found"));

        codenvyLicenseManager.store(LICENSE_TEXT);

        verify(codenvyLicenseDao).store(actionCaptor.capture());
        CodenvyLicenseActionImpl action = actionCaptor.getValue();
        assertEquals(action.getLicenseType(), PRODUCT_LICENSE);
        assertEquals(action.getActionType(), ACCEPTED);
        assertFalse(isNullOrEmpty(action.getLicenseQualifier()));

        verify(codenvyLicense).getLicenseText();
        assertTrue(Files.exists(licenseFile.toPath()));
        assertEquals(LICENSE_TEXT, readFileToString(licenseFile, UTF_8));
    }

    /**
     * Use case:
     *  - user adds the same license twice
     * Verify:
     *  - license action is stored in the DB only once
     */
    @Test
    public void shouldStoreSameLicenseTwice() throws Exception {
        CodenvyLicenseActionImpl action = mock(CodenvyLicenseActionImpl.class);
        when(action.getLicenseQualifier()).thenReturn(LICENSE_QUALIFIER);
        when(codenvyLicenseDao.getByLicenseAndType(PRODUCT_LICENSE, ACCEPTED))
                .thenThrow(new NotFoundException("Codenvy license action not found"))
                .thenReturn(action);

        codenvyLicenseManager.store(LICENSE_TEXT);

        codenvyLicenseManager.store(LICENSE_TEXT);
        verify(codenvyLicenseDao, times(1)).store(any(CodenvyLicenseActionImpl.class));
    }

    /**
     * Use case:
     *  - user stores license
     *  - user stores a new license
     * Verify:
     *  - a new license replaces an old one in the file
     *  - new license action replaces an old one in the DB
     */
    @Test
    public void shouldUpdateLicense() throws Exception {
        CodenvyLicenseActionImpl action = mock(CodenvyLicenseActionImpl.class);
        when(action.getLicenseQualifier()).thenReturn(LICENSE_QUALIFIER);
        when(codenvyLicenseDao.getByLicenseAndType(PRODUCT_LICENSE, ACCEPTED))
                .thenThrow(new NotFoundException("Codenvy license action not found"))
                .thenReturn(action);

        codenvyLicenseManager.store(LICENSE_TEXT);

        verify(codenvyLicenseDao, times(1)).store(any(CodenvyLicenseActionImpl.class));

        codenvyLicenseManager.store(NEW_LICENSE_TEXT);

        assertTrue(Files.exists(licenseFile.toPath()));
        assertEquals(NEW_LICENSE_TEXT, readFileToString(licenseFile, UTF_8));
        verify(codenvyLicenseDao).remove(PRODUCT_LICENSE);
        verify(codenvyLicenseDao, times(2)).store(any(CodenvyLicenseActionImpl.class));
    }

    /**
     * Use case:
     *  - user stores license
     *  - user removes license
     * Verify:
     *  - license file is absent
     *  - license {@link ACCEPTED} action isn't removed in the DB
     *  - license {@link EXPIRED} action is added in the DB
     */
    @Test
    public void shouldRemoveLicense() throws Exception {
        CodenvyLicenseActionImpl action = mock(CodenvyLicenseActionImpl.class);
        when(action.getLicenseQualifier()).thenReturn(LICENSE_QUALIFIER);
        when(codenvyLicenseDao.getByLicenseAndType(PRODUCT_LICENSE, ACCEPTED))
                .thenThrow(new NotFoundException("Codenvy license action not found"))
                .thenReturn(action);

        codenvyLicenseManager.store(LICENSE_TEXT);
        codenvyLicenseManager.delete();

        assertFalse(Files.exists(licenseFile.toPath()));
        verify(codenvyLicenseDao, never()).remove(eq(PRODUCT_LICENSE));

        ArgumentCaptor<CodenvyLicenseActionImpl> actionCaptor = ArgumentCaptor.forClass(CodenvyLicenseActionImpl.class);
        verify(codenvyLicenseDao, times(2)).store(actionCaptor.capture());
        CodenvyLicenseActionImpl expireAction = actionCaptor.getAllValues().get(1);
        assertEquals(expireAction.getLicenseType(), PRODUCT_LICENSE);
        assertEquals(expireAction.getActionType(), EXPIRED);
        assertEquals(expireAction.getLicenseQualifier(), LICENSE_QUALIFIER);
    }

    @Test(expectedExceptions = LicenseNotFoundException.class)
    public void shouldThrowLicenseNotFoundExceptionIfWeTryToDeleteLicenseFromEmptyStorage() {
        codenvyLicenseManager.delete();
    }

    @Test
    public void shouldLoadLicense() {
        when(licenseFactory.create(LICENSE_TEXT)).thenReturn(codenvyLicense);
        codenvyLicenseManager.store(LICENSE_TEXT);

        CodenvyLicense license = codenvyLicenseManager.load();

        verify(licenseFactory, times(2)).create(LICENSE_TEXT);
        assertEquals(license, codenvyLicense);
    }

    @Test(expectedExceptions = LicenseNotFoundException.class)
    public void shouldThrowLicenseNotFoundExceptionIfWeTryToGetLicenseFromEmptyStorage() {
        codenvyLicenseManager.load();
    }

    @Test
    public void testIsCodenvyLicenseUsageLegal() throws IOException, ServerException {
        doReturn(mockCodenvyLicense).when(codenvyLicenseManager).load();
        doReturn(true).when(mockCodenvyLicense).isLicenseUsageLegal(USER_NUMBER, NODES_NUMBER);

        assertTrue(codenvyLicenseManager.isCodenvyUsageLegal());
    }

    @Test
    public void testIsCodenvyFreeUsageLegal() throws IOException, ServerException {
        when(userManager.getTotalCount()).thenReturn(MAX_NUMBER_OF_FREE_USERS);
        setSizeOfAdditionalNodes(MAX_NUMBER_OF_FREE_SERVERS);

        doThrow(LicenseNotFoundException.class).when(codenvyLicenseManager).load();

        assertTrue(codenvyLicenseManager.isCodenvyUsageLegal());
    }

    @Test
    public void testIsCodenvyLicenseUsageNotLegal() throws IOException, ServerException {
        doReturn(mockCodenvyLicense).when(codenvyLicenseManager).load();
        doReturn(false).when(mockCodenvyLicense).isLicenseUsageLegal(USER_NUMBER, NODES_NUMBER);

        assertFalse(codenvyLicenseManager.isCodenvyUsageLegal());
    }

    @Test
    public void testIsCodenvyFreeUsageNotLegal() throws IOException, ServerException {
        when(userManager.getTotalCount()).thenReturn(MAX_NUMBER_OF_FREE_USERS + 1);
        setSizeOfAdditionalNodes(MAX_NUMBER_OF_FREE_SERVERS + 1);

        doThrow(LicenseNotFoundException.class).when(codenvyLicenseManager).load();

        assertFalse(codenvyLicenseManager.isCodenvyUsageLegal());
    }

    @Test
    public void testIsCodenvyActualNodesUsageLegal() throws IOException, ServerException {
        doReturn(mockCodenvyLicense).when(codenvyLicenseManager).load();
        doReturn(true).when(mockCodenvyLicense).isLicenseNodesUsageLegal(NODES_NUMBER);

        assertTrue(codenvyLicenseManager.isCodenvyNodesUsageLegal(null));
    }

    @Test
    public void testIsCodenvyGivenNodesUsageLegal() throws IOException, ServerException {
        doReturn(mockCodenvyLicense).when(codenvyLicenseManager).load();
        doReturn(true).when(mockCodenvyLicense).isLicenseNodesUsageLegal(NODES_NUMBER);

        assertTrue(codenvyLicenseManager.isCodenvyNodesUsageLegal(NODES_NUMBER));
        verify(swarmDockerConnector, never()).getAvailableNodes();
    }

    @Test
    public void testIsCodenvyActualNodesUsageNotLegal() throws IOException, ServerException {
        doReturn(mockCodenvyLicense).when(codenvyLicenseManager).load();
        doReturn(false).when(mockCodenvyLicense).isLicenseNodesUsageLegal(NODES_NUMBER);

        assertFalse(codenvyLicenseManager.isCodenvyNodesUsageLegal(null));
    }

    @Test
    public void testIsCodenvyGivenNodesUsageNotLegal() throws IOException, ServerException {
        doReturn(mockCodenvyLicense).when(codenvyLicenseManager).load();
        doReturn(false).when(mockCodenvyLicense).isLicenseNodesUsageLegal(NODES_NUMBER);

        assertFalse(codenvyLicenseManager.isCodenvyNodesUsageLegal(NODES_NUMBER));
        verify(swarmDockerConnector, never()).getAvailableNodes();
    }

    @Test
    public void testIsCodenvyNodesFreeUsageLegal() throws IOException, ServerException {
        setSizeOfAdditionalNodes(MAX_NUMBER_OF_FREE_SERVERS);

        doThrow(LicenseNotFoundException.class).when(codenvyLicenseManager).load();

        assertTrue(codenvyLicenseManager.isCodenvyNodesUsageLegal(null));
    }

    @Test
    public void testIsCodenvyNodesFreeUsageNotLegal() throws IOException, ServerException {
        setSizeOfAdditionalNodes(MAX_NUMBER_OF_FREE_SERVERS + 1);

        doThrow(LicenseNotFoundException.class).when(codenvyLicenseManager).load();

        assertFalse(codenvyLicenseManager.isCodenvyNodesUsageLegal(null));
    }

    private void setSizeOfAdditionalNodes(int size) throws IOException {
        when(swarmDockerConnector.getAvailableNodes()).thenReturn(dockerNodes);
        when(dockerNodes.size()).thenReturn(size);
    }

}
