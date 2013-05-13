package com.codenvy.analytics.ldap;

import com.codenvy.organization.client.UserManager;
import com.codenvy.organization.exception.OrganizationServiceException;
import com.codenvy.organization.model.Profile;

/**
 * Simple wrapper for {@link UserManager} to perform read-only operations. Application server hosting LDAP back-end URL should be defined in
 * the system property: "organization.application.server.url"
 */
public class ReadOnlyUserManager {

    UserManager userManager;

    public ReadOnlyUserManager() throws OrganizationServiceException {
        super();
        this.userManager = new UserManager();
    }

    public ReadOnlyUserProfile getUserProfile(String alias) throws OrganizationServiceException {
        Profile ldapUserProfile = userManager.getUserProfile(alias);

        return new ReadOnlyUserProfile(ldapUserProfile.getAttribute("firstName"), ldapUserProfile.getAttribute("lastName"),
                                       ldapUserProfile.getAttribute("phone"), ldapUserProfile.getAttribute("employer"));
    }
}
