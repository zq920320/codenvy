/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2013] Codenvy, S.A. 
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
package com.codenvy.factory;

import com.codenvy.api.factory.*;
import com.codenvy.api.factory.dto.Factory;
import com.codenvy.commons.lang.URLEncodedUtils;
import com.codenvy.organization.client.AccountManager;
import com.codenvy.organization.client.UserManager;
import com.codenvy.organization.exception.OrganizationServiceException;
import com.codenvy.organization.model.Account;
import com.codenvy.organization.model.User;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Validates parameters of factory
 *
 * @author Alexander Garagatyi
 */
public class FactoryUrlBaseValidator implements FactoryUrlValidator {

    private static final Pattern PROJECT_NAME_VALIDATOR = Pattern.compile("^[\\\\\\w\\\\\\d]+[\\\\\\w\\\\\\d_.-]*$");

    private AccountManager accountManager;

    private UserManager userManager;

    private FactoryBuilder factoryBuilder;

    @Inject
    public FactoryUrlBaseValidator(AccountManager accountManager, UserManager userManager, FactoryBuilder factoryBuilder) {
        this.accountManager = accountManager;
        this.userManager = userManager;
        this.factoryBuilder = factoryBuilder;
    }

    @Override
    public void validateUrl(URI factoryUrl) throws FactoryUrlException {
        Map<String, Set<String>> params = URLEncodedUtils.parse(factoryUrl, "UTF-8");

        if (params.get("id") != null) {
            if (params.get("id").size() != 1) {
                throw new FactoryUrlException("Parameters must not has multiple values");
            }
            FactoryClient factoryClient = new HttpFactoryClient(factoryUrl.getScheme(), factoryUrl.getHost(), factoryUrl.getPort());
            Factory factory = factoryClient.getFactory(params.get("id").iterator().next());
            factory = factoryBuilder.convertToLatest(factory);
            this.validateObject(factory, true);
        } else {
            Factory factory = factoryBuilder.buildNonEncoded(factoryUrl);
            this.validateObject(factoryBuilder.convertToLatest(factory), false);
        }
    }

    @Override
    public void validateObject(Factory factory, boolean encoded) throws FactoryUrlException {
        validateCommonParams(factory);

        if (encoded) {
            if (factory.getUserid() != null) {
                try {
                    User user = userManager.getUserById(factory.getUserid());
                    if (user.isTemporary()) {
                        throw new FactoryUrlException("Current user is not allowed for using this method.");
                    }
                    if (factory.getWelcome() != null) {
                        String orgid = factory.getOrgid();
                        Account account = accountManager.getAccountById(orgid);
                        if (!account.getOwner().getId().equals(user.getId())) {
                            throw new FactoryUrlException("You are not authorized to use this orgid.");
                        }
                    }
                } catch (OrganizationServiceException e) {
                    throw new FactoryUrlException("Unable to validate user " + factory.getUserid());
                }
            }
            if (factory.getWelcome() != null && (factory.getOrgid() == null || factory.getOrgid().isEmpty())) {
                throw new FactoryUrlException("Using a custom Welcome Page requires a valid orgid parameter.");
            }
        }
    }

    protected void validateCommonParams(Factory factoryUrl) throws FactoryUrlException {
        // check that vcs value is correct (only git is supported for now)
        if (!"git".equals(factoryUrl.getVcs())) {
            throw new FactoryUrlException("Parameter 'vcs' has illegal value. Only \"git\" is supported for now.");
        }
        if (factoryUrl.getVcsurl() == null || factoryUrl.getVcsurl().isEmpty()) {
            throw new FactoryUrlException("Parameter 'vcsurl' is null or empty.");
        } else {
            try {
                URLDecoder.decode(factoryUrl.getVcsurl(), "UTF-8");
            } catch (IllegalArgumentException | UnsupportedEncodingException e) {
                throw new FactoryUrlException("Parameter 'vcsurl' has illegal value.");
            }
        }

        // validate orgid
        if (factoryUrl.getOrgid() != null && !factoryUrl.getOrgid().isEmpty()) {
            try {
                Account account = accountManager.getAccountById(factoryUrl.getOrgid());

                String endTime;
                if ("Managed Factory".equals(account.getAttribute("tariff_plan")) &&
                    (endTime = account.getAttribute("tariff_end_time")) != null) {
                    Date endTimeDate = new Date(Long.valueOf(endTime));
                    if (endTimeDate.after(new Date())) {
                        return;
                    }
                }
            } catch (OrganizationServiceException | NumberFormatException ignore) {
            }
            throw new FactoryUrlException("Parameter orgid is invalid.");
        }

        String pname;
        if (factoryUrl.getProjectattributes() != null && (pname = factoryUrl.getProjectattributes().getPname()) != null) {
            if (!PROJECT_NAME_VALIDATOR.matcher(pname).matches()) {
                throw new FactoryUrlException(
                        "Project name must contain only Latin letters, digits or these following special characters -._.");
            }
        }
    }
}
