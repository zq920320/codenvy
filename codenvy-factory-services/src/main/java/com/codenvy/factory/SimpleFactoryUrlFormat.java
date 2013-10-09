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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Simple version of <code>FactoryUrlFormat</code>.
 * This implementation suggest that factory url contain all required parameters
 */
public class SimpleFactoryUrlFormat implements FactoryUrlFormat {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleFactoryUrlFormat.class);

    protected static final String SUPPORT_EMAIL   = "support@codenvy.com";
    protected static final String DEFAULT_MESSAGE =
            String.format("We cannot locate your project. Please try again or contact %s.", SUPPORT_EMAIL);

    private final static List<String> mandatoryParameters;

    // do not inline into pattern, it's used by IDE and they have no Pattern class
    public static final String WSO_2_URL_STRING = "(http|https):\\/\\/((([0-9a-fA-F]{32}(:x-oauth-basic){0,1})|([0-9a-zA-Z-_.]+))@){0,1}" +
                                                  "gitblit\\.codeenvy.com(:[0-9]{1,5}){0,1}/.*\\.git";

    public static final Pattern WSO_2_URL_PATTERN = Pattern.compile(WSO_2_URL_STRING);

    // Required factory url parameters
    static {
        mandatoryParameters = new LinkedList<>();
        // v parameter should be checked in another way to satisfy v 1.1 url schema
        mandatoryParameters.add("vcs");
        mandatoryParameters.add("vcsurl");
        mandatoryParameters.add("idcommit");
    }

    @Override
    public SimpleFactoryUrl parse(URL url) throws FactoryUrlException {
        try {
            Map<String, List<String>> params = UrlUtils.getQueryParameters(url);

            // check API version first
            List<String> versionValues = params.get("v");
            if (versionValues != null && versionValues.size() > 1) {
                throw new FactoryUrlInvalidArgumentException(DEFAULT_MESSAGE);
            } else if (versionValues == null || !"1.0".equals(versionValues.iterator().next())) {
                throw new FactoryUrlInvalidFormatException(DEFAULT_MESSAGE);
            }

            // check mandatory parameters
            for (String paramToCheck : mandatoryParameters) {
                List<String> values = params.get(paramToCheck);
                if (values == null) {
                    throw new FactoryUrlInvalidArgumentException(DEFAULT_MESSAGE);
                } else {
                    // throw exception if parameter quantity greater than one
                    // Also throw exception if parameter value is null or empty
                    String value = values.size() == 1 ? values.iterator().next() : null;
                    if (value == null || value.isEmpty()) {
                        throw new FactoryUrlInvalidArgumentException(DEFAULT_MESSAGE);
                    }

                    // check that vcs value is correct (only git is supported for now)
                    if ("vcs".equals(paramToCheck) && !"git".equals(value)) {
                        throw new FactoryUrlInvalidArgumentException("Parameter vcs has illegal value. Only \"git\" is supported for now.");
                    }
                }
            }

            // check that vcs value is correct (only git is supported for now)
            if (!"git".equals(params.get("vcs").iterator().next())) {
                throw new FactoryUrlInvalidArgumentException("Parameter vcs has illegal value. Only \"git\" is supported for now.");
            }

            checkRepository(params.get("vcsurl").iterator().next());

            SimpleFactoryUrl factoryUrl = new SimpleFactoryUrl();
            factoryUrl.setCommitid(params.get("idcommit").iterator().next());
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

            if ((values = params.get("keepvcsinfo")) != null && !values.isEmpty()) {
                factoryUrl.setKeepvcsinfo(Boolean.parseBoolean(values.iterator().next()));
            }

            Map<String, String> projectAttributes = new HashMap<>();
            if ((values = params.get("pname")) != null && !values.isEmpty()) {
                projectAttributes.put("pname", values.iterator().next());
            }
            if ((values = params.get("ptype")) != null && !values.isEmpty()) {
                projectAttributes.put("ptype", values.iterator().next());
            }

            factoryUrl.setProjectattributes(projectAttributes);

            return factoryUrl;
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new FactoryUrlException(DEFAULT_MESSAGE);
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
            //Temporary case, to check if we have git url from wso2.
            //For private repository "git ls-remote" will be frozen to prompt user credentials.
            if (WSO_2_URL_PATTERN.matcher(vcsUrl).matches()) {
                LOG.debug("WSO2 repository found. Checked finished.");
                return;
            }

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
                                      SUPPORT_EMAIL));
            } else {
                LOG.debug("Repository check finished successfully.");
            }
        } catch (InterruptedException | IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new FactoryUrlException(e.getLocalizedMessage(), e);
        }
    }
}
