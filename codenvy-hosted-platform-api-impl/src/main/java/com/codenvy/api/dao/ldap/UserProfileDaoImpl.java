/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.dao.Profile;
import org.eclipse.che.api.user.server.dao.UserProfileDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.InitialLdapContext;


import static java.lang.String.format;

/**
 * LDAP based implementation of {@link UserProfileDao}
 *
 * @author Eugene Voevodin
 */
public class UserProfileDaoImpl implements UserProfileDao {

    private static final Logger LOG = LoggerFactory.getLogger(UserProfileDaoImpl.class);

    private final InitialLdapContextFactory contextFactory;
    private final ProfileAttributesMapper   attributesMapper;

    @Inject
    public UserProfileDaoImpl(InitialLdapContextFactory contextFactory, ProfileAttributesMapper attributesMapper) {
        this.contextFactory = contextFactory;
        this.attributesMapper = attributesMapper;
    }

    /**
     * Not supported for this implementation, nothing will be done
     */
    @Override
    public void create(Profile profile) throws ServerException {
    }

    /**
     * Not supported for this implementation, nothing will be done
     */
    @Override
    public void remove(String id) throws ServerException {
    }

    /** {@inheritDoc} */
    @Override
    public void update(Profile profile) throws NotFoundException, ServerException {
        final String id = profile.getId();
        final Profile existing = getById(id);
        InitialLdapContext context = null;
        try {
            final ModificationItem[] mods = attributesMapper.createModifications(existing.getAttributes(), profile.getAttributes());
            if (mods.length > 0) {
                context = contextFactory.createContext();
                context.modifyAttributes(attributesMapper.getProfileDn(id), mods);
            }
        } catch (NamingException ex) {
            throw new ServerException(format("Unable to update profile '%s'", profile.getId()));
        } finally {
            close(context);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Profile getById(String id) throws NotFoundException, ServerException {
        try {
            final Profile profile = doGetById(id);
            if (profile == null) {
                throw new NotFoundException(format("Profile with id '%s' was not found", id));
            }
            return profile;
        } catch (NamingException namingEx) {
            throw new ServerException(format("Unable to get profile '%s'", id), namingEx);
        }
    }

    private Profile doGetById(String id) throws NamingException {
        Profile profile = null;
        InitialLdapContext context = null;
        try {
            context = contextFactory.createContext();
            final Attributes attributes = getProfileAttributes(context, id);
            if (attributes != null) {
                profile = attributesMapper.asProfile(attributes);
            }
        } finally {
            close(context);
        }
        return profile;
    }

    private Attributes getProfileAttributes(InitialLdapContext ctx, String id) throws NamingException {
        try {
            return ctx.getAttributes(attributesMapper.getProfileDn(id));
        } catch (NameNotFoundException nnfEx) {
            return null;
        }
    }

    private void close(InitialLdapContext context) {
        if (context != null) {
            try {
                context.close();
            } catch (NamingException namingEx) {
                LOG.error(namingEx.getMessage(), namingEx);
            }
        }
    }
}
