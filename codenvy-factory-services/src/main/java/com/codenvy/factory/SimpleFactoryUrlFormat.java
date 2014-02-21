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
import com.codenvy.api.factory.dto.SimpleFactoryUrl;
import com.codenvy.api.factory.dto.Variable;
import com.codenvy.commons.json.JsonHelper;
import com.codenvy.commons.json.JsonParseException;
import com.codenvy.commons.lang.UrlUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Simple version of <code>FactoryUrlFormat</code>.
 * This implementation suggest that factory url contain all required parameters
 */
public class SimpleFactoryUrlFormat implements FactoryUrlFormat<SimpleFactoryUrl> {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleFactoryUrlFormat.class);

    protected static final String SUPPORT_EMAIL   = "support@codenvy.com";
    protected static final String DEFAULT_MESSAGE =
            String.format("We cannot locate your project. Please try again or contact %s.", SUPPORT_EMAIL);

    private final static List<String> mandatoryParameters;

    // Required factory url parameters
    static {
        mandatoryParameters = new LinkedList<>();
        // v parameter should be checked in another way to satisfy v 1.1 url schema
        mandatoryParameters.add("vcs");
        mandatoryParameters.add("vcsurl");
    }

    @Override
    public SimpleFactoryUrl parse(URL url) throws FactoryUrlException {
        try {
            Map<String, List<String>> params = UrlUtils.getQueryParameters(url);

            // check API version first
            List<String> versionValues = params.get("v");
            if (versionValues != null && versionValues.size() > 1) {
                throw new FactoryUrlException(DEFAULT_MESSAGE);
            } else if (versionValues == null || !"1.0".equals(versionValues.iterator().next())) {
                throw new FactoryUrlException(DEFAULT_MESSAGE);
            }

            // check mandatory parameters
            for (String paramToCheck : mandatoryParameters) {
                List<String> values = params.get(paramToCheck);
                if (values == null) {
                    throw new FactoryUrlException(DEFAULT_MESSAGE);
                } else {
                    // throw exception if parameter quantity greater than one
                    // Also throw exception if parameter value is null or empty
                    String value = values.size() == 1 ? values.iterator().next() : null;
                    if (value == null || value.isEmpty()) {
                        throw new FactoryUrlException(DEFAULT_MESSAGE);
                    }
                }
            }

            SimpleFactoryUrl factoryUrl = new SimpleFactoryUrlImpl();
            factoryUrl.setV(params.get("v").iterator().next());
            factoryUrl.setVcs(params.get("vcs").iterator().next());
            factoryUrl.setVcsurl(params.get("vcsurl").iterator().next());

            List<String> values;
            if ((values = params.get("action")) != null && !values.isEmpty()) {
                factoryUrl.setAction(values.iterator().next());
            }

            if ((values = params.get("openfile")) != null && !values.isEmpty()) {
                factoryUrl.setOpenfile(values.iterator().next());
            }

            if ((values = params.get("vcsinfo")) != null && !values.isEmpty()) {
                factoryUrl.setVcsinfo(Boolean.parseBoolean(values.iterator().next()));
            }

            if ((values = params.get("idcommit")) != null && !values.isEmpty()) {
                factoryUrl.setCommitid(values.iterator().next());
            }

            if ((values = params.get("vcsbranch")) != null && !values.isEmpty()) {
                factoryUrl.setVcsbranch(values.iterator().next());
            }

            Map<String, String> projectAttributes = new HashMap<>();
            if ((values = params.get("pname")) != null && !values.isEmpty()) {
                String pname = values.iterator().next();
                projectAttributes.put("pname", pname);
            }
            if ((values = params.get("ptype")) != null && !values.isEmpty()) {
                projectAttributes.put("ptype", values.iterator().next());
            }

            factoryUrl.setProjectattributes(projectAttributes);

            if ((values = params.get("variables")) != null && !values.isEmpty()) {
                factoryUrl.setVariables(
                        Arrays.asList(JsonHelper.fromJson(values.iterator().next(), Variable[].class, null)));
            }

            return factoryUrl;
        } catch (IOException | JsonParseException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new FactoryUrlException(DEFAULT_MESSAGE);
        }
    }
}
