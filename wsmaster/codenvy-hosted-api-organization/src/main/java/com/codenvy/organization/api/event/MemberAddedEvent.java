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

import static com.codenvy.organization.shared.event.EventType.MEMBER_ADDED;

/**
 * Defines the event of adding the organization member.
 *
 * @author Anton Korneta
 */
public class MemberAddedEvent implements OrganizationEvent {

    private final String performerName;
    private final String addedUserId;
    private final String organizationId;

    public MemberAddedEvent(String performerName,
                            String addedUserId,
                            String organizationId) {
        this.performerName = performerName;
        this.addedUserId = addedUserId;
        this.organizationId = organizationId;
    }

    @Override
    public String getOrganizationId() {
        return organizationId;
    }

    @Override
    public EventType getType() {
        return MEMBER_ADDED;
    }

    public String getPerformerName() {
        return performerName;
    }

    public String getAddedUserId() {
        return addedUserId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MemberAddedEvent)) {
            return false;
        }
        final MemberAddedEvent that = (MemberAddedEvent)obj;
        return Objects.equals(performerName, that.performerName)
               && Objects.equals(addedUserId, that.addedUserId)
               && Objects.equals(organizationId, that.organizationId);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(performerName);
        hash = 31 * hash + Objects.hashCode(addedUserId);
        hash = 31 * hash + Objects.hashCode(organizationId);
        return hash;
    }

    @Override
    public String toString() {
        return "MemberAddedEvent{" +
               "organizationId='" + getOrganizationId() + '\'' +
               ", eventType='" + getType() + '\'' +
               ", performerName='" + performerName + '\'' +
               ", addedUserId='" + addedUserId + '\'' +
               '}';
    }

}
