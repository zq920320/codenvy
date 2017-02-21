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
import com.codenvy.organization.shared.model.Organization;

import java.util.Objects;

import static com.codenvy.organization.shared.event.EventType.ORGANIZATION_RENAMED;

/**
 * Defines organization renamed event.
 *
 * @author Anton Korneta
 */
public class OrganizationRenamedEvent implements OrganizationEvent {

    private final String       performerName;
    private final String       oldName;
    private final String       newName;
    private final Organization organization;

    public OrganizationRenamedEvent(String performerName,
                                    String oldName,
                                    String newName,
                                    Organization organization) {
        this.performerName = performerName;
        this.oldName = oldName;
        this.newName = newName;
        this.organization = organization;
    }

    public String getPerformerName() {
        return performerName;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }

    public Organization getOrganization() {
        return organization;
    }

    @Override
    public String getOrganizationId() {
        return organization.getId();
    }

    @Override
    public EventType getType() {
        return ORGANIZATION_RENAMED;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OrganizationRenamedEvent)) {
            return false;
        }
        final OrganizationRenamedEvent that = (OrganizationRenamedEvent)obj;
        return Objects.equals(performerName, that.performerName)
               && Objects.equals(oldName, that.oldName)
               && Objects.equals(newName, that.newName)
               && Objects.equals(organization, that.organization);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(performerName);
        hash = 31 * hash + Objects.hashCode(oldName);
        hash = 31 * hash + Objects.hashCode(newName);
        hash = 31 * hash + Objects.hashCode(organization);
        return hash;
    }

    @Override
    public String toString() {
        return "OrganizationRenamedEvent{" +
               "organizationId='" + getOrganizationId() + '\'' +
               ", eventType='" + getType() + '\'' +
               ", performerName='" + performerName + '\'' +
               ", oldName='" + oldName + '\'' +
               ", newName='" + newName + '\'' +
               ", organization=" + organization +
               '}';
    }

}
