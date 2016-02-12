/*
 *  [2012] - [2016] Codenvy, S.A.
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

import com.google.inject.Singleton;

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores available subscription services
 *
 * @author Eugene Voevodin
 */
@Singleton
public class SubscriptionServiceRegistry {

    private final Map<String, AbstractSubscriptionService> services;

    @Inject
    public SubscriptionServiceRegistry(Set<AbstractSubscriptionService> services) {
        this.services = new ConcurrentHashMap<>();
        for (AbstractSubscriptionService service : services) {
            add(service);
        }
    }

    public void add(AbstractSubscriptionService service) {
        services.put(service.getServiceId(), service);
    }

    public AbstractSubscriptionService get(String serviceId) {
        if (serviceId == null) {
            return null;
        }
        return services.get(serviceId);
    }

    public AbstractSubscriptionService remove(String serviceId) {
        if (serviceId == null) {
            return null;
        }
        return services.remove(serviceId);
    }

    public Set<AbstractSubscriptionService> getAll() {
        return new LinkedHashSet<>(services.values());
    }

    public void clear() {
        services.clear();
    }
}