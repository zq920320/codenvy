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
import com.codenvy.organization.shared.model.Member;
import com.codenvy.organization.shared.model.Organization;

import java.util.List;
import java.util.Objects;

import static com.codenvy.organization.shared.event.EventType.ORGANIZATION_REMOVED;

/**
 * Defines organization removed event.
 *
 * @author Anton Korneta
 */
public class OrganizationRemovedEvent implements OrganizationEvent {

    private final String                 performerName;
    private final Organization           organization;
    private final List<? extends Member> members;

    public OrganizationRemovedEvent(String performerName,
                                    Organization organization,
                                    List<? extends Member> members) {
        this.performerName = performerName;
        this.organization = organization;
        this.members = members;
    }

    public String getPerformerName() {
        return performerName;
    }

    public Organization getOrganization() {
        return organization;
    }

    public List<? extends Member> getMembers() {
        return members;
    }

    @Override
    public String getOrganizationId() {
        return organization.getId();
    }

    @Override
    public EventType getType() {
        return ORGANIZATION_REMOVED;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OrganizationRemovedEvent)) {
            return false;
        }
        final OrganizationRemovedEvent that = (OrganizationRemovedEvent)obj;
        return Objects.equals(performerName, that.performerName)
               && Objects.equals(organization, that.organization)
               && getMembers().equals(that.getMembers());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(performerName);
        hash = 31 * hash + Objects.hashCode(organization);
        hash = 31 * hash + getMembers().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "OrganizationRemovedEvent{" +
               "organizationId='" + getOrganizationId() + '\'' +
               ", eventType='" + getType() + '\'' +
               ", performerName='" + performerName + '\'' +
               ", members=" + members +
               ", organization=" + organization +
               '}';
    }

}
