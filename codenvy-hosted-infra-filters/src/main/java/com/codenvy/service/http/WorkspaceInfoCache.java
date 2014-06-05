/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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

package com.codenvy.service.http;

import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.rest.HttpJsonHelper;
import com.codenvy.api.core.rest.shared.dto.Link;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.api.workspace.shared.dto.Attribute;
import com.codenvy.api.workspace.shared.dto.Workspace;
import com.codenvy.dto.server.DtoFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.name.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Cache ~ 1000 Workspace entries for 10 minutes.
 *
 * @author Sergii Kabashniuk
 */
@Singleton
public class WorkspaceInfoCache {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceInfoCache.class);

    private final LoadingCache<Key, Workspace> workspaceCache;


    @Inject
    public WorkspaceInfoCache(WorkspaceCacheLoader cacheLoader) {
        this.workspaceCache = CacheBuilder.newBuilder()
                                          .maximumSize(1000)
                                          .expireAfterWrite(10, TimeUnit.MINUTES)
                                          .build(cacheLoader);

    }


    /**
     * @param wsName
     *         - workspace name
     * @return - workspace entry
     * @throws ServerException
     * @throws NotFoundException
     */
    public Workspace getByName(String wsName) throws ServerException, NotFoundException {
        try {
            return doGet(new Key(wsName, false));
        } catch (ExecutionException e) {
            if (e.getCause() instanceof NotFoundException) {
                throw ((NotFoundException)e.getCause());
            } else if (e.getCause() instanceof ServerException) {
                throw ((ServerException)e.getCause());
            }
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @param id
     *         - workspace id.
     * @return - workspace entry
     * @throws ServerException
     * @throws NotFoundException
     */
    public Workspace getById(String id) throws ServerException, NotFoundException {
        try {
            return doGet(new Key(id, true));
        } catch (ExecutionException e) {
            if (e.getCause() instanceof NotFoundException) {
                throw ((NotFoundException)e.getCause());
            } else if (e.getCause() instanceof ServerException) {
                throw ((ServerException)e.getCause());
            }
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }


    /**
     * Remove workspace from cache by workspace id.
     *
     * @param id
     *         - id of workspace to remove.
     */
    public void removeById(String id) {
        workspaceCache.invalidate(new Key(id, true));
    }

    /**
     * Remove workspace by workspace name.
     *
     * @param wsName
     *         - name workspace to remove
     */
    public void removeByName(String wsName) {
        workspaceCache.invalidate(new Key(wsName, false));
    }

    private Workspace doGet(Key key) throws ServerException, NotFoundException, ExecutionException {
        Workspace workspace = workspaceCache.get(key);
        if (workspace.isTemporary()) {
            for (Attribute attribute : workspace.getAttributes()) {
                if (attribute.getName().equals("allowAnyoneAddMember")) {
                    return workspace;
                }
            }
            workspaceCache.invalidate(key);
            workspaceCache
                    .invalidate(key.isUuid ? new Key(workspace.getName(), false) : new Key(workspace.getId(), true));
            workspace = workspaceCache.get(key);
        }
        return workspace;

    }

    public abstract static class WorkspaceCacheLoader extends CacheLoader<Key, Workspace> {

    }

    /**
     * Cachloader that gets Workspace from DAO
     */
    public static class DaoWorkspaceCacheLoader extends WorkspaceCacheLoader {
        @Inject
        WorkspaceDao dao;

        @Override
        public Workspace load(Key key) throws Exception {
            LOG.debug("Load {} from dao ", key.key);
            try {
                return key.isUuid ? dao.getById(key.key) : dao.getByName(key.key);
            } catch (Exception e) {
                LOG.debug(e.getLocalizedMessage(), e);
                throw e;
            }
        }
    }

    /**
     * Cachloader that gets Workspace from API
     */
    public static class HttpWorkspaceCacheLoader extends WorkspaceCacheLoader {

        @Inject
        @Named("api.endpoint")
        String apiEndpoint;

        @Override
        public Workspace load(Key key) throws Exception {
            LOG.debug("Load {} from dao ", key.key);
            try {
                Link getWorkspaceLink = null;
                if (key.isUuid) {

                    getWorkspaceLink =
                            DtoFactory.getInstance().createDto(Link.class).withMethod("GET")
                                      .withHref(apiEndpoint + "/workspace/" + key.key);
                } else {
                    getWorkspaceLink =
                            DtoFactory.getInstance().createDto(Link.class).withMethod("GET")
                                      .withHref(apiEndpoint + "/workspace?name=" + key.key);
                }

                return HttpJsonHelper.request(Workspace.class, getWorkspaceLink);
            } catch (Exception e) {
                LOG.debug(e.getLocalizedMessage(), e);
                throw e;
            }
        }
    }

    private class Key {
        final String  key;
        final boolean isUuid;

        private Key(String key, boolean isUuid) {
            this.key = key;
            this.isUuid = isUuid;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key1 = (Key)o;

            if (isUuid != key1.isUuid) return false;
            if (!key.equals(key1.key)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = key.hashCode();
            result = 31 * result + (isUuid ? 1 : 0);
            return result;
        }
    }
}
