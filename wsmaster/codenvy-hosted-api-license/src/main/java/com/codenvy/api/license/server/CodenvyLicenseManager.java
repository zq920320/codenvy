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
import com.codenvy.api.license.InvalidLicenseException;
import com.codenvy.api.license.LicenseException;
import com.codenvy.api.license.LicenseNotFoundException;
import com.codenvy.api.license.model.Constants;
import com.codenvy.api.license.model.FairSourceLicenseAcceptance;
import com.codenvy.api.license.server.dao.CodenvyLicenseDao;
import com.codenvy.api.license.server.model.impl.CodenvyLicenseActionImpl;
import com.codenvy.swarm.client.SwarmDockerConnector;
import com.google.common.hash.Hashing;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.codenvy.api.license.model.Constants.Action.ACCEPTED;
import static com.codenvy.api.license.model.Constants.Action.EXPIRED;
import static com.codenvy.api.license.model.Constants.Type.FAIR_SOURCE_LICENSE;
import static com.codenvy.api.license.model.Constants.Type.PRODUCT_LICENSE;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 * @author Alexander Andrienko
 */
@Singleton
public class CodenvyLicenseManager {

    private final CodenvyLicenseFactory licenseFactory;
    private final Path                  licenseFile;
    private final UserManager           userManager;
    private final SwarmDockerConnector  dockerConnector;
    private final CodenvyLicenseDao     codenvyLicenseDao;

    @Inject
    public CodenvyLicenseManager(@Named("license-manager.license-file") String licenseFile,
                                 CodenvyLicenseFactory licenseFactory,
                                 UserManager userManager,
                                 SwarmDockerConnector dockerConnector,
                                 CodenvyLicenseDao codenvyLicenseDao) {
        this.licenseFactory = licenseFactory;
        this.licenseFile = Paths.get(licenseFile);
        this.userManager = userManager;
        this.dockerConnector = dockerConnector;
        this.codenvyLicenseDao = codenvyLicenseDao;
    }

    /**
     * Stores valid Codenvy license into the storage.
     *
     * @throws NullPointerException
     *         if {@code licenseText} is null
     * @throws LicenseException
     *         if error occurred while storing
     */
    public void store(@NotNull String licenseText) throws LicenseException, ApiException {
        requireNonNull(licenseText, "Codenvy license can't be null");

        CodenvyLicense codenvyLicense = licenseFactory.create(licenseText);
        try {
            Files.write(licenseFile, codenvyLicense.getLicenseText().getBytes());
        } catch (IOException e) {
            throw new LicenseException(e.getMessage(), e);
        }

        String licenseQualifier = calculateLicenseMD5sum(licenseText);
        try {
            CodenvyLicenseActionImpl licenseAction = codenvyLicenseDao.getByLicenseAndType(PRODUCT_LICENSE, ACCEPTED);
            if (!licenseAction.getLicenseQualifier().equals(licenseQualifier)) {
                codenvyLicenseDao.remove(PRODUCT_LICENSE, ACCEPTED);
                codenvyLicenseDao.remove(PRODUCT_LICENSE, EXPIRED);
                addLicenseAction(PRODUCT_LICENSE, ACCEPTED, licenseQualifier);
            }
        } catch (NotFoundException e) {
            addLicenseAction(PRODUCT_LICENSE, ACCEPTED, licenseQualifier);
        }
    }

    /**
     * Loads Codenvy license out of underlying storage.
     *
     * @throws LicenseNotFoundException
     *         if license not found
     * @throws InvalidLicenseException
     *         if license not valid
     * @throws LicenseException
     *         if error occurred while loading license
     */
    @Nullable
    public CodenvyLicense load() throws LicenseException {
        String licenseText = readLicenseText();
        if (isNullOrEmpty(licenseText)) {
            throw new LicenseNotFoundException("Codenvy license not found");
        }

        return licenseFactory.create(licenseText);
    }

    /**
     * Deletes Codenvy license from the storage.
     *
     * @throws LicenseException
     *         if error occurred while deleting license
     */
    public void delete() throws LicenseException, ApiException {
        String licenseText = readLicenseText();
        String licenseQualifier = calculateLicenseMD5sum(licenseText);

        try {
            Files.delete(licenseFile);
        } catch (NoSuchFileException e) {
            throw new LicenseNotFoundException("Codenvy license not found");
        } catch (IOException e) {
            throw new LicenseException(e.getMessage(), e);
        }

        codenvyLicenseDao.remove(PRODUCT_LICENSE, EXPIRED);
        addLicenseAction(PRODUCT_LICENSE, EXPIRED, licenseQualifier);
    }

    /**
     * Return true if only Codenvy usage meets the constrains of license properties or free usage properties.
     **/
    public boolean isCodenvyUsageLegal() throws ServerException, IOException {
        long actualUsers = userManager.getTotalCount();
        int actualServers = dockerConnector.getAvailableNodes().size();

        try {
            CodenvyLicense codenvyLicense = load();
            return codenvyLicense.isLicenseUsageLegal(actualUsers, actualServers);
        } catch (LicenseException e) {
            return CodenvyLicense.isFreeUsageLegal(actualUsers, actualServers);
        }
    }

    /**
     * Return true if only node number meets the constrains of license properties or free usage properties.
     * If nodeNumber == null, uses actual number of machine nodes.
     *
     * @param nodeNumber
     *         number of machine nodes.
     */
    public boolean isCodenvyNodesUsageLegal(Integer nodeNumber) throws IOException {
        if (nodeNumber == null) {
            nodeNumber = dockerConnector.getAvailableNodes().size();
        }

        try {
            CodenvyLicense codenvyLicense = load();
            return codenvyLicense.isLicenseNodesUsageLegal(nodeNumber);
        } catch (LicenseException e) {
            return CodenvyLicense.isFreeUsageLegal(0, nodeNumber);  // user number doesn't matter
        }
    }

    /**
     * Accepts Codenvy Fair Source License
     *
     * @see CodenvyLicenseDao#store(CodenvyLicenseActionImpl)
     *
     * @param fairSourceLicenseAcceptance
     *      acceptance request
     * @throws ConflictException
     *      if license already has been accepted
     * @throws BadRequestException
     *      if request is not complete
     */
    public void acceptFairSourceLicense(FairSourceLicenseAcceptance fairSourceLicenseAcceptance) throws ApiException {
        try {
            codenvyLicenseDao.getByLicenseAndType(FAIR_SOURCE_LICENSE, ACCEPTED);
            throw new ConflictException("Codenvy Fair Source License has been already accepted");
        } catch (NotFoundException e) {
            // No Codenvy Fair Source License Accepted
        }

        validateAcceptFairSourceLicenseRequest(fairSourceLicenseAcceptance);

        Map<String, String> attributes = new HashMap<>(3);
        attributes.put("firstName", fairSourceLicenseAcceptance.getFirstName());
        attributes.put("lastName", fairSourceLicenseAcceptance.getLastName());
        attributes.put("email", fairSourceLicenseAcceptance.getEmail());
        addLicenseAction(FAIR_SOURCE_LICENSE, ACCEPTED, null, attributes);
    }

    /**
     * Indicates if Codenvy Fair Source License is accepted.
     */
    public boolean hasAcceptedFairSourceLicense() throws ServerException {
        try {
            codenvyLicenseDao.getByLicenseAndType(FAIR_SOURCE_LICENSE, ACCEPTED);
        } catch (NotFoundException e) {
            return false;
        }

        return true;
    }

    private void addLicenseAction(Constants.Type licenseType,
                                  Constants.Action actionType,
                                  @Nullable String licenseQualifier) throws ApiException {
        addLicenseAction(licenseType, actionType, licenseQualifier, emptyMap());
    }

    private void addLicenseAction(Constants.Type licenseType,
                                  Constants.Action actionType,
                                  @Nullable String licenseQualifier,
                                  Map<String, String> attributes) throws ApiException {
        CodenvyLicenseActionImpl codenvyLicenseAction = new CodenvyLicenseActionImpl(licenseType,
                                                                                     actionType,
                                                                                     System.currentTimeMillis(),
                                                                                     licenseQualifier,
                                                                                     attributes);
        codenvyLicenseDao.store(codenvyLicenseAction);
    }

    private void validateAcceptFairSourceLicenseRequest(FairSourceLicenseAcceptance fairSourceLicenseAcceptance)
            throws BadRequestException {
        String email = fairSourceLicenseAcceptance.getEmail();

        if (isNullOrEmpty(email)
            || isNullOrEmpty(fairSourceLicenseAcceptance.getFirstName())
            || isNullOrEmpty(fairSourceLicenseAcceptance.getLastName())) {

            throw new BadRequestException("Codenvy Fair Source License can't be accepted until all fields are filled.");
        }

        try {
            InternetAddress internetAddress = new InternetAddress(email);
            internetAddress.validate();
        } catch (AddressException e) {
            throw new BadRequestException(format("Codenvy Fair Source License can't be accepted until. Email %s is not valid", email));
        }
    }

    private String calculateLicenseMD5sum(@NotNull String licenseText) {
        return Hashing.md5().hashString(licenseText, defaultCharset()).toString();
    }

    private String readLicenseText() throws LicenseException {
        try {
            return new String(Files.readAllBytes(licenseFile), UTF_8);
        } catch (NoSuchFileException e) {
            throw new LicenseNotFoundException("Codenvy license not found");
        } catch (IOException e) {
            throw new LicenseException(e.getMessage(), e);
        }
    }
}
