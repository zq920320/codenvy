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
package com.codenvy.workspace;

import com.codenvy.api.core.*;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.user.server.dao.*;
import com.codenvy.api.user.shared.dto.*;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.api.workspace.shared.dto.Workspace;
import com.codenvy.workspace.event.StopWsEvent;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

/**
 * Cache removal listener that remove temporary workspace and its memberships, temporary users.
 *
 * @author Alexander Garagatyi
 */
public class WorkspaceRemovalListener implements RemovalListener<String, Boolean> {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceRemovalListener.class);

    private final EventService eventService;

    private final WorkspaceDao workspaceDao;

    private final MemberDao memberDao;

    private final UserDao userDao;

    private final UserProfileDao userProfileDao;

    @Inject
    public WorkspaceRemovalListener(EventService eventService, WorkspaceDao workspaceDao, MemberDao memberDao, UserDao userDao,
                                    UserProfileDao userProfileDao) {
        this.eventService = eventService;
        this.workspaceDao = workspaceDao;
        this.memberDao = memberDao;
        this.userDao = userDao;
        this.userProfileDao = userProfileDao;
    }

    @Override
    public void onRemoval(RemovalNotification<String, Boolean> notification) {
        if (notification.getValue()) {
            try {
                String wsId = notification.getKey();
                Workspace workspace = workspaceDao.getById(wsId);
                final List<Member> members = memberDao.getWorkspaceMembers(wsId);
                for (Member member : members) {
                    memberDao.remove(member);
                }
                workspaceDao.remove(wsId);

                LOG.info("EVENT#workspace-destroyed# WS#{}#", workspace.getName());

                for (Member member : members) {
                    Profile userProfile = userProfileDao.getById(member.getUserId());
                    for (Attribute attr : userProfile.getAttributes()) {
                        if ("temporary".equals(attr.getName()) && "true".equals(attr.getValue())) {
                            userDao.remove(member.getUserId());
                        }
                    }
                }
            } catch (ConflictException | NotFoundException | ServerException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        } else {
            eventService.publish(new StopWsEvent());
        }
    }
}
