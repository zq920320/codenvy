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
import com.codenvy.commons.lang.UrlUtils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Garagatyi
 */
public class FactoryUrlValidator {
    public static void validate(URL factoryUrl) throws FactoryUrlException {
        try {
            Map<String, List<String>> params = UrlUtils.getQueryParameters(factoryUrl);

            FactoryUrlFormat factoryUrlFormat;
            if (params.get("id") != null) {
                factoryUrlFormat = new AdvancedFactoryUrlFormat();
            } else {
                factoryUrlFormat = new SimpleFactoryUrlFormat();
            }

            factoryUrlFormat.parse(factoryUrl);
        } catch (UnsupportedEncodingException e) {
            throw new FactoryUrlException(e.getLocalizedMessage(), e);
        }
    }
}
