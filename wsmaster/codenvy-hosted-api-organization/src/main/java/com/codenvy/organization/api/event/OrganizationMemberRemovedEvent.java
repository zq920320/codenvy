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
package com.codenvy.organization.api.event;

import com.codenvy.organization.shared.event.EventType;
import com.codenvy.organization.shared.event.OrganizationEvent;

import java.util.Objects;

/**
 * Defines the event for organization member removal.
 *
 * @author Anton Korneta
 */
public class OrganizationMemberRemovedEvent implements OrganizationEvent {

    private final String organizationManagerName;
    private final String removedUserId;
    private final String organizationId;


    public OrganizationMemberRemovedEvent(String organizationManagerName,
                                          String removedUserId,
                                          String organizationId) {
        this.organizationManagerName = organizationManagerName;
        this.removedUserId = removedUserId;
        this.organizationId = organizationId;
    }

    @Override
    public EventType getType() {
        return EventType.ORGANIZATION_MEMBER_REMOVED;
    }

    @Override
    public String getOrganizationId() {
        return organizationId;
    }

    public String getOrganizationManagerName() {
        return organizationManagerName;
    }

    public String getRemovedUserId() {
        return removedUserId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OrganizationMemberRemovedEvent)) {
            return false;
        }
        final OrganizationMemberRemovedEvent that = (OrganizationMemberRemovedEvent)obj;
        return Objects.equals(organizationManagerName, that.organizationManagerName)
               && Objects.equals(removedUserId, that.removedUserId)
               && Objects.equals(organizationId, that.organizationId);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(organizationManagerName);
        hash = 31 * hash + Objects.hashCode(removedUserId);
        hash = 31 * hash + Objects.hashCode(organizationId);
        return hash;
    }

    @Override
    public String toString() {
        return "OrganizationMemberRemovedEvent{" +
               "organizationId='" + getOrganizationId() + '\'' +
               ", eventType='" + getType() + '\'' +
               ", organizationManagerName='" + organizationManagerName + '\'' +
               ", userId='" + removedUserId + '\'' +
               '}';
    }

}
