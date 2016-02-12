/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.api.dao.ldap;

import com.codenvy.api.user.server.dao.AdminUserDao;
import com.google.inject.Inject;

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.List;

import static com.google.api.client.repackaged.com.google.common.base.Preconditions.checkArgument;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class AdminUserDaoImpl extends UserDaoImpl implements AdminUserDao {

    private static final Logger LOG = LoggerFactory.getLogger(AdminUserDaoImpl.class);
    private final UserLdapPagination userLdapPagination;

    @Inject
    public AdminUserDaoImpl(AccountDao accountDao,
                            UserProfileDao profileDao,
                            PreferenceDao preferenceDao,
                            InitialLdapContextFactory contextFactory,
                            @Named("user.ldap.user_container_dn") String userContainerDn,
                            @Named("user.ldap.user_dn") String userDn,
                            @Named("user.ldap.old_user_dn") String oldUserDn,
                            UserAttributesMapper userAttributesMapper,
                            EventService eventService,
                            UserLdapPagination userLdapPagination) {
        super(accountDao,
              profileDao,
              preferenceDao,
              contextFactory,
              userContainerDn,
              userDn,
              oldUserDn,
              userAttributesMapper,
              eventService);
        this.userLdapPagination = userLdapPagination;
    }


    @Override
    public List<User> getAll(int maxItems, int skipCount) throws ServerException, IllegalArgumentException {
        checkArgument(maxItems >= 0, "The number of items to return can't be negative.");
        checkArgument(skipCount >= 0, "The number of items to skip can't be negative.");

        try {
            return userLdapPagination.get(maxItems, skipCount);
        } catch (NamingException | IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServerException("Unable get all users.", e);
        }
    }
}
