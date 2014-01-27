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
import com.codenvy.commons.lang.UrlUtils;
import com.codenvy.organization.client.AccountManager;
import com.codenvy.organization.exception.OrganizationServiceException;
import com.codenvy.organization.model.Account;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    @Inject
    public FactoryUrlBaseValidator(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    @Override
    public void validateUrl(URL url) throws FactoryUrlException {
        try {
            Map<String, List<String>> params = UrlUtils.getQueryParameters(url);

            FactoryUrlFormat factoryUrlFormat;
            if (params.get("id") != null) {
                factoryUrlFormat = new AdvancedFactoryUrlFormat(new HttpFactoryClient(url.getProtocol(), url.getHost(), url.getPort()));
            } else {
                factoryUrlFormat = new SimpleFactoryUrlFormat();
            }

            this.validateUrl(factoryUrlFormat.parse(url));
        } catch (UnsupportedEncodingException e) {
            throw new FactoryUrlException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void validateUrl(SimpleFactoryUrl factoryUrl) throws FactoryUrlException {
        // check mandatory parameters
        if (!"1.0".equals(factoryUrl.getV())) {
            throw new FactoryUrlException("Version has illegal value. Version must be equal to '1.0'");
        }

        validateCommonParams(factoryUrl);
    }

    @Override
    public void validateUrl(AdvancedFactoryUrl factoryUrl) throws FactoryUrlException {
        // check mandatory parameters
        if (!"1.1".equals(factoryUrl.getV())) {
            throw new FactoryUrlException("Version has illegal value. Version must be equal to '1.1'");
        }

        validateCommonParams(factoryUrl);
    }

    protected void validateCommonParams(SimpleFactoryUrl factoryUrl) throws FactoryUrlException {
        // check that vcs value is correct (only git is supported for now)
        if (!"git".equals(factoryUrl.getVcs())) {
            throw new FactoryUrlException("Parameter vcs has illegal value. Only \"git\" is supported for now.");
        }
        if (factoryUrl.getVcsurl() == null || factoryUrl.getVcsurl().isEmpty()) {
            throw new FactoryUrlException("Parameter vcsurl is null or empty.");
        }

        // validate orgid
        if (factoryUrl.getOrgid() != null && !factoryUrl.getOrgid().isEmpty()) {
            try {
                Account account = accountManager.getAccountById(factoryUrl.getOrgid());

                String endTime;
                if ((endTime = account.getAttribute("tariff_end_time")) != null) {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date endTimeDate = df.parse(endTime);
                    if (endTimeDate.after(new Date())) {
                        return;
                    }
                }
            } catch (OrganizationServiceException | ParseException ignore) {
            }
            throw new FactoryUrlException("Parameter orgid is invalid.");
        }

        String pname;
        if (factoryUrl.getProjectattributes() != null && (pname = factoryUrl.getProjectattributes().get("pname")) != null) {
            if (!PROJECT_NAME_VALIDATOR.matcher(pname).matches()) {
                throw new FactoryUrlException(
                        "Project name must contain only Latin letters, digits or these following special characters -._.");
            }
        }
    }
}
