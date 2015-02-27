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
package com.codenvy.workspace.listener;

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.user.server.dao.PreferenceDao;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.workspace.server.dao.Member;
import com.codenvy.api.workspace.server.dao.MemberDao;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.workspace.event.StopWsEvent;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static java.lang.Boolean.parseBoolean;

/**
 * Cache removal listener that remove temporary workspace and its memberships, temporary users.
 *
 * @author Alexander Garagatyi
 */
public class WorkspaceRemovalListener implements RemovalListener<String, Boolean> {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceRemovalListener.class);

    private final EventService  eventService;
    private final WorkspaceDao  workspaceDao;
    private final MemberDao     memberDao;
    private final UserDao       userDao;
    private final PreferenceDao preferenceDao;

    @Inject
    public WorkspaceRemovalListener(EventService eventService,
                                    WorkspaceDao workspaceDao,
                                    MemberDao memberDao,
                                    UserDao userDao,
                                    PreferenceDao preferenceDao) {
        this.eventService = eventService;
        this.workspaceDao = workspaceDao;
        this.memberDao = memberDao;
        this.userDao = userDao;
        this.preferenceDao = preferenceDao;
    }

    @Override
    public void onRemoval(RemovalNotification<String, Boolean> notification) {
        try {
            if (notification.getValue()) {
                try {
                    String wsId = notification.getKey();
                    final List<Member> members;
                    try {
                        members = memberDao.getWorkspaceMembers(wsId);
                        workspaceDao.remove(wsId);
                    } catch (NotFoundException e) {
                        return;
                    }

                    for (Member member : members) {
                        final Map<String, String> preferences = preferenceDao.getPreferences(member.getUserId());
                        if (parseBoolean(preferences.get("temporary"))) {
                            userDao.remove(member.getUserId());
                        }
                    }
                } catch (ConflictException | NotFoundException | ServerException e) {
                    LOG.warn(e.getLocalizedMessage());
                }
            }
        } finally {
            eventService.publish(new StopWsEvent(notification.getKey(), notification.getValue()));
            LOG.info("Workspace is stopped. Id#{}# Temporary#{}#", notification.getKey(), notification.getValue());
        }
    }
}
