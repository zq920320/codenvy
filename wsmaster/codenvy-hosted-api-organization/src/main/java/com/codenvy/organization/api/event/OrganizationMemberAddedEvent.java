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

import static com.codenvy.organization.shared.event.EventType.ORGANIZATION_MEMBER_ADDED;

/**
 * Defines the event of adding the organization member.
 *
 * @author Anton Korneta
 */
public class OrganizationMemberAddedEvent implements OrganizationEvent {

    private final String referrerName;
    private final String addedUserId;
    private final String organizationId;

    public OrganizationMemberAddedEvent(String referrerName,
                                        String addedUserId,
                                        String organizationId) {
        this.referrerName = referrerName;
        this.addedUserId = addedUserId;
        this.organizationId = organizationId;
    }

    @Override
    public String getOrganizationId() {
        return organizationId;
    }

    @Override
    public EventType getType() {
        return ORGANIZATION_MEMBER_ADDED;
    }

    public String getReferrerName() {
        return referrerName;
    }

    public String getAddedUserId() {
        return addedUserId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OrganizationMemberAddedEvent)) {
            return false;
        }
        final OrganizationMemberAddedEvent that = (OrganizationMemberAddedEvent)obj;
        return Objects.equals(referrerName, that.referrerName)
               && Objects.equals(addedUserId, that.addedUserId)
               && Objects.equals(organizationId, that.organizationId);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(referrerName);
        hash = 31 * hash + Objects.hashCode(addedUserId);
        hash = 31 * hash + Objects.hashCode(organizationId);
        return hash;
    }

    @Override
    public String toString() {
        return "OrganizationMemberAddedEvent{" +
               "organizationId='" + getOrganizationId() + '\'' +
               ", eventType='" + getType() + '\'' +
               ", referrerName='" + referrerName + '\'' +
               ", addedUserId='" + addedUserId + '\'' +
               '}';
    }

}
