package com.codenvy.analytics.ldap;

import com.codenvy.organization.client.UserManager;
import com.codenvy.organization.exception.OrganizationServiceException;
import com.codenvy.organization.exception.UserExistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * Simple wrapper for {@link UserManager} to perform read-only operations. Application server hosting LDAP back-end URL should be defined in
 * the system property: "organization.application.server.url"
 */
public class ReadOnlyUserManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadOnlyUserManager.class);

    private final UserManager   delegatedManager;

    public ReadOnlyUserManager() throws OrganizationServiceException {
        this.delegatedManager = new UserManager();
    }

    /**
     * For testing purpose.
     */
    public ReadOnlyUserManager(UserManager userManager) throws OrganizationServiceException {
        this.delegatedManager = userManager;
    }

    /**
     * Returns user attributes by user alias. If user does not exist then empty map will be returned.
     */
    public Map<String, String> getUserAttributes(String alias) throws OrganizationServiceException {
        try {
            return delegatedManager.getUserProfile(alias).getAttributes();
        } catch (UserExistenceException e) {
            LOGGER.warn(e.getMessage() + " for " + alias);
            return Collections.emptyMap();
        }
    }
}
