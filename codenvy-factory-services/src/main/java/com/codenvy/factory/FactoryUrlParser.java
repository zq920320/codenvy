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

import com.codenvy.api.factory.FactoryUrlException;
import com.codenvy.api.factory.SimpleFactoryUrl;
import com.codenvy.commons.lang.UrlUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/** Class to parse factory url parameters and validate them */
public class FactoryUrlParser {
    private static final Logger LOG = LoggerFactory.getLogger(FactoryUrlParser.class);

    /**
     * Validate and parse factory url
     *
     * @param factoryUrl
     *         - factory url to parse
     * @throws FactoryUrlInvalidFormatException
     *         - there is no format to validate such url
     * @throws FactoryUrlInvalidArgumentException
     *         - if url satisfy format, but arguments is invalid
     * @throws FactoryUrlException
     *         - if other exceptions occurs
     */
    public static SimpleFactoryUrl parse(URL factoryUrl) throws FactoryUrlException {
        try {
            Map<String, List<String>> params = UrlUtils.getQueryParameters(factoryUrl);

            FactoryUrlFormat factoryUrlFormat;
            if (params.get("id") != null) {
                factoryUrlFormat = new AdvancedFactoryUrlFormat();
            } else {
                factoryUrlFormat = new SimpleFactoryUrlFormat();
            }

            return factoryUrlFormat.parse(factoryUrl);
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new FactoryUrlException(SimpleFactoryUrlFormat.DEFAULT_MESSAGE);
        }
    }
}
