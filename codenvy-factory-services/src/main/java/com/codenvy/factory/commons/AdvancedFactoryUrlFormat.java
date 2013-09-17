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
package com.codenvy.factory.commons;

import com.codenvy.commons.lang.UrlUtils;
import com.codenvy.factory.client.FactoryClient;
import com.codenvy.factory.client.impl.HttpFactoryClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Advanced version of <code>FactoryUrlFormat</code>.
 * This implementation suggest that factory url contain version and id
 */
public class AdvancedFactoryUrlFormat implements FactoryUrlFormat {
    private static final Logger LOG = LoggerFactory.getLogger(AdvancedFactoryUrlFormat.class);
    private final static List<String>  mandatoryParameters;
    // client for retrieving factory parameters from storage
    private final        FactoryClient factoryClient;

    // Required factory url parameters
    static {
        mandatoryParameters = new LinkedList<>();
        mandatoryParameters.add("v");
        mandatoryParameters.add("id");
    }

    public AdvancedFactoryUrlFormat() {
        this.factoryClient = new HttpFactoryClient();
    }

    AdvancedFactoryUrlFormat(FactoryClient factoryClient) {
        this.factoryClient = factoryClient;
    }

    @Override
    public AdvancedFactoryUrl parse(URL url) throws FactoryUrlException {
        try {
            Map<String, List<String>> params = UrlUtils.getQueryParameters(url);

            // check API version first
            List<String> versionValues = params.get("v");
            if (versionValues == null) {
                throw new FactoryUrlInvalidArgumentException(SimpleFactoryUrlFormat.DEFAULT_MESSAGE);
            } else if (!versionValues.contains("1.1")) {
                throw new FactoryUrlInvalidFormatException(SimpleFactoryUrlFormat.DEFAULT_MESSAGE);
            }

            // check mandatory parameters
            for (String paramToCheck : mandatoryParameters) {
                List<String> values = params.get(paramToCheck);
                if (values == null) {
                    throw new FactoryUrlInvalidArgumentException(SimpleFactoryUrlFormat.DEFAULT_MESSAGE);
                } else {
                    // throw exception if parameter quantity greater than one
                    // Also throw exception if parameter value is null or empty
                    String value = values.size() == 1 ? values.iterator().next() : null;
                    if (value == null || value.isEmpty()) {
                        throw new FactoryUrlInvalidArgumentException(SimpleFactoryUrlFormat.DEFAULT_MESSAGE);
                    }
                }
            }

            AdvancedFactoryUrl factoryUrl = factoryClient.getFactory(url, params.get("id").iterator().next());

            // check that vcs value is correct (only git is supported for now)
            if (!"git".equals(factoryUrl.getVcs())) {
                throw new FactoryUrlInvalidArgumentException("Parameter vcs has illegal value. Only \"git\" is supported for now.");
            }

            checkRepository(factoryUrl.getVcsUrl());

            return factoryUrl;
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new FactoryUrlException(SimpleFactoryUrlFormat.DEFAULT_MESSAGE);
        }
    }

    /**
     * Check git repository for a project existence and availability
     *
     * @param vcsUrl
     *         - git repository url
     * @throws FactoryUrlInvalidArgumentException
     *         - if repository isn't accessible
     * @throws FactoryUrlException
     *         - if other exceptions occurs
     */
    protected static void checkRepository(String vcsUrl) throws FactoryUrlException {
        try {
            // To avoid repository cloning use git ls-remote util for repository check
            // Call git ls-remote is much faster than cloning
            Process process = Runtime.getRuntime().exec("/usr/bin/git ls-remote " + vcsUrl);

            // check return value of process.
            if (process.waitFor() != 0) {
                LOG.error("Can't check repository {}. Exit value is {}", new Object[][]{{vcsUrl, process.exitValue()}});
                BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                String line;
                while ((line = br.readLine()) != null) {
                    LOG.error(line);
                }
                throw new FactoryUrlInvalidArgumentException(
                        String.format("We cannot clone the git repository for your project. Please try again or contact %s.",
                                      SimpleFactoryUrlFormat.SUPPORT_EMAIL));
            } else {
                LOG.debug("Repository check finished successfully.");
            }
        } catch (InterruptedException | IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new FactoryUrlException(e.getLocalizedMessage(), e);
        }
    }
}
