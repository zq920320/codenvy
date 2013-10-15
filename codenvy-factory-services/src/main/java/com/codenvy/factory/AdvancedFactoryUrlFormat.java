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

import com.codenvy.api.factory.AdvancedFactoryUrl;
import com.codenvy.api.factory.AdvancedFactoryUrlValidator;
import com.codenvy.api.factory.FactoryUrlException;
import com.codenvy.commons.lang.UrlUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Advanced version of <code>FactoryUrlFormat</code>.
 * This implementation suggest that factory url contain id
 */
public class AdvancedFactoryUrlFormat implements FactoryUrlFormat, AdvancedFactoryUrlValidator {
    private static final Logger LOG = LoggerFactory.getLogger(AdvancedFactoryUrlFormat.class);
    // client for retrieving factory parameters from storage
    private final FactoryClient factoryClient;

    public AdvancedFactoryUrlFormat() {
        this.factoryClient = new HttpFactoryClient();
    }

    AdvancedFactoryUrlFormat(FactoryClient factoryClient) {
        this.factoryClient = factoryClient;
    }

    @Override
    public AdvancedFactoryUrl parse(URL url) throws FactoryUrlException {
        try {
            String factoryId;
            List<String> values = UrlUtils.getQueryParameters(url).get("id");
            if (values != null && !values.isEmpty()) {
                factoryId = values.iterator().next();
            } else {
                throw new FactoryUrlInvalidFormatException(SimpleFactoryUrlFormat.DEFAULT_MESSAGE);
            }

            AdvancedFactoryUrl factoryUrl = factoryClient.getFactory(url, factoryId);

            if (factoryUrl == null) {
                LOG.error("Can't find factory with id {}", factoryId);
                throw new FactoryUrlInvalidArgumentException(SimpleFactoryUrlFormat.DEFAULT_MESSAGE);
            }

            validate(factoryUrl);

            return factoryUrl;
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new FactoryUrlException(SimpleFactoryUrlFormat.DEFAULT_MESSAGE);
        }
    }

    @Override
    public void validate(AdvancedFactoryUrl factoryUrl) throws FactoryUrlException {
        // check mandatory parameters
        if (!"1.1".equals(factoryUrl.getV())) {
            throw new FactoryUrlInvalidFormatException("Version has illegal value. Version must be equal to '1.1'");
        }
        // check that vcs value is correct (only git is supported for now)
        if (!"git".equals(factoryUrl.getVcs())) {
            throw new FactoryUrlInvalidArgumentException(
                    "Parameter vcs has illegal value. Only \"git\" is supported for now.");
        }
        if (factoryUrl.getVcsurl() == null || factoryUrl.getVcsurl().isEmpty()) {
            throw new FactoryUrlInvalidArgumentException("Vcsurl is null or empty.");
        }

        SimpleFactoryUrlFormat.checkRepository(factoryUrl.getVcsurl());
    }
}
