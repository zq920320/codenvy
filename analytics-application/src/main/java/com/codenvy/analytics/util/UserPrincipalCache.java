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
package com.codenvy.analytics.util;

import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** @author Anatoliy Bazko */
@Singleton
public class UserPrincipalCache {

    private static final Logger LOG                 = LoggerFactory.getLogger(UserPrincipalCache.class);
    private static final long   EXPIRATION_INTERVAL = 60 * 60 * 1000; // 1h
    public static final  long   MAX_ENTRIES         = 1000;

    private final Map<String, UserContext> cache;

    public UserPrincipalCache() {
        this.cache = new ConcurrentHashMap<>();
        CacheCleaner cacheCleaner = new CacheCleaner();
        cacheCleaner.setDaemon(true);
        cacheCleaner.start();

        LOG.info("UserPrincipalCache is initialized");
    }

    public UserContext get(Principal principal) {
        return cache.get(principal.getName());
    }

    public boolean exist(Principal principal) {
        return cache.containsKey(principal.getName());
    }

    public void put(Principal principal, UserContext userContext) {
        cache.put(principal.getName(), userContext);
    }

    public int size() {
        return cache.size();
    }

    public void clear() {
        cache.clear();
    }

    public void updateAccessTime(Principal principal) {
        UserContext userContext = cache.get(principal.getName());
        if (userContext != null) {
            userContext.updateAccessTime();
        }
    }

    public static class UserContext {
        private long lastAccessTime;

        private final Set<String> allowedUsers;
        private final Set<String> allowedWorkspaces;

        public UserContext(Set<String> allowedUsers, Set<String> allowedWorkspaces) {
            this.allowedUsers = allowedUsers;
            this.allowedWorkspaces = allowedWorkspaces;
            this.lastAccessTime = System.currentTimeMillis();
        }

        public Set<String> getAllowedUsers() {
            return allowedUsers;
        }

        public Set<String> getAllowedWorkspaces() {
            return allowedWorkspaces;
        }

        private void updateAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }

        private boolean isExpired() {
            return System.currentTimeMillis() > lastAccessTime + EXPIRATION_INTERVAL;
        }
    }

    private class CacheCleaner extends Thread {

        private CacheCleaner() {
            super("UserPrincipal cache cleaner");
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                for (Map.Entry<String, UserContext> entry : cache.entrySet()) {
                    if (entry.getValue().isExpired()) {
                        cache.remove(entry.getValue());
                    }
                }

                if (cache.size() > MAX_ENTRIES) {
                    List<Map.Entry<String, UserContext>> entries = new ArrayList<>(cache.entrySet());
                    Collections.sort(entries, new Comparator<Map.Entry<String, UserContext>>() {
                        @Override
                        public int compare(Map.Entry<String, UserContext> o1, Map.Entry<String, UserContext> o2) {
                            if (o1.getValue().lastAccessTime < o2.getValue().lastAccessTime) {
                                return -1;
                            } else if (o1.getValue().lastAccessTime > o2.getValue().lastAccessTime) {
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                    });

                    for (int i = 0; i < Math.max(0, entries.size() - MAX_ENTRIES); i++) {
                        cache.remove(entries.get(i).getKey());
                    }
                }

                try {
                    sleep(60 * 1000);
                } catch (InterruptedException e) {
                    break;
                }

                LOG.info("UserPrincipalCache size is " + cache.size());
            }

            LOG.warn("CacheCleaner thread is stopped");
        }
    }
}
