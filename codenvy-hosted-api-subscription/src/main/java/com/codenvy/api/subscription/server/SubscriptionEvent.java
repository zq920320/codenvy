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
package com.codenvy.api.subscription.server;

import com.codenvy.api.subscription.server.dao.Subscription;

import org.eclipse.che.api.core.notification.EventOrigin;

/**
 * @author Sergii Leschenko
 */
@EventOrigin("subscription")
public class SubscriptionEvent {
    public enum EventType {
        ADDED("added"),
        REMOVED("removed"),
        RENEWED("renewed");

        private final String value;

        EventType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private EventType    type;
    private Subscription subscription;

    public SubscriptionEvent(EventType type, Subscription subscription) {
        this.type = type;
        this.subscription = subscription;
    }

    public EventType getType() {
        return type;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public static SubscriptionEvent subscriptionAddedEvent(Subscription subscription) {
        return new SubscriptionEvent(EventType.ADDED, subscription);
    }

    public static SubscriptionEvent subscriptionRemovedEvent(Subscription subscription) {
        return new SubscriptionEvent(EventType.REMOVED, subscription);
    }

    public static SubscriptionEvent subscriptionRenewedEvent(Subscription subscription) {
        return new SubscriptionEvent(EventType.RENEWED, subscription);
    }
}
