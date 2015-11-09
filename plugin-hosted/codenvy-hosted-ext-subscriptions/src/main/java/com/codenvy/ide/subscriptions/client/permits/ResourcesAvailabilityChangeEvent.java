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
package com.codenvy.ide.subscriptions.client.permits;


import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that describes the fact that lock or unlock resources happened.
 *
 * @author Igor Vinokur
 */
public class ResourcesAvailabilityChangeEvent extends GwtEvent<ResourcesAvailabilityChangeHandler> {
    public static Type<ResourcesAvailabilityChangeHandler> TYPE = new Type<>();
    private boolean resourcesAvailable;

    /**
     * Create new {@link ResourcesAvailabilityChangeEvent}.
     *
     * @param resourcesAvailable true if resources are available and false if they are locked
     */
    public ResourcesAvailabilityChangeEvent(boolean resourcesAvailable) {
        this.resourcesAvailable = resourcesAvailable;
    }

    @Override
    public Type<ResourcesAvailabilityChangeHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ResourcesAvailabilityChangeHandler resourcesAvailabilityChangeHandler) {
        resourcesAvailabilityChangeHandler.onResourcesAvailabilityChange(resourcesAvailable);
    }
}
