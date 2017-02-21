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
package com.codenvy.api.permission.server.event;

import com.codenvy.api.permission.shared.event.EventType;
import com.codenvy.api.permission.shared.event.PermissionsEvent;
import com.codenvy.api.permission.shared.model.Permissions;

import static com.codenvy.api.permission.shared.event.EventType.PERMISSIONS_REMOVED;

/**
 * Defines permissions added events.
 *
 * @author Anton Korneta
 */
public class PermissionsRemovedEvent implements PermissionsEvent {

    private final Permissions permissions;
    private final String      usernameWhoRemovePermissions;

    public PermissionsRemovedEvent(Permissions permissions,
                                   String usernameWhoRemovePermissions) {
        this.permissions = permissions;
        this.usernameWhoRemovePermissions = usernameWhoRemovePermissions;
    }

    @Override
    public Permissions getPermissions() {
        return permissions;
    }

    public String getUsernameWhoRemovePermissions() {
        return usernameWhoRemovePermissions;
    }

    @Override
    public EventType getType() {
        return PERMISSIONS_REMOVED;
    }

}
