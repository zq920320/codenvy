/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.organization.api.listener;

import com.codenvy.api.permission.server.event.PermissionsAddedEvent;
import com.codenvy.api.permission.server.event.PermissionsRemovedEvent;
import com.codenvy.api.permission.shared.event.PermissionsEvent;
import com.codenvy.api.permission.shared.model.Permissions;
import com.codenvy.organization.api.OrganizationManager;
import com.codenvy.organization.api.event.OrganizationMemberAddedEvent;
import com.codenvy.organization.api.event.OrganizationMemberRemovedEvent;
import com.codenvy.organization.api.permissions.OrganizationDomain;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.user.server.UserManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Maps permissions to organization related events.
 *
 * @author Anton Korneta
 */
@Singleton
public class MemberEventsMapper implements EventSubscriber<PermissionsEvent> {

    private final EventService        eventService;
    private final UserManager         userManager;
    private final OrganizationManager organizationManager;

    @Inject
    public MemberEventsMapper(EventService eventService,
                              UserManager userManager,
                              OrganizationManager organizationManager) {
        this.eventService = eventService;
        this.userManager = userManager;
        this.organizationManager = organizationManager;
    }

    @PostConstruct
    private void subscribe(EventService eventService) {
        eventService.subscribe(this);
    }

    @PreDestroy
    private void unsubscribe(EventService eventService) {
        eventService.subscribe(this);
    }

    @Override
    public void onEvent(PermissionsEvent event) {
        final Permissions permissions = event.getPermissions();
        if (permissions != null && OrganizationDomain.DOMAIN_ID.equals(permissions.getDomainId())) {
            try {
                String orgName = organizationManager.getById(event.getPermissions().getInstanceId()).getName();
                try {
                    userManager.getByName(orgName);
                } catch (NotFoundException ignore) {
                    switch (event.getType()) {
                        case PERMISSIONS_ADDED:
                            String referrer = ((PermissionsAddedEvent)event).getUsernameWhoAddPermissions();
                            eventService.publish(new OrganizationMemberAddedEvent(referrer,
                                                                                  permissions.getUserId(),
                                                                                  permissions.getInstanceId()));
                            break;
                        case PERMISSIONS_REMOVED:
                            String remover = ((PermissionsRemovedEvent)event).getUsernameWhoRemovePermissions();
                            eventService.publish(new OrganizationMemberRemovedEvent(remover,
                                                                                    permissions.getUserId(),
                                                                                    permissions.getInstanceId()));
                    }
                }
            } catch (NotFoundException | ServerException ignored) {
            }
        }
    }
}
