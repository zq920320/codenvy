/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.view;

import com.codenvy.analytics.metrics.TimeUnit;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public interface View {

    /**
     * Updates view accordingly to new {@link TimeUnit} parameter.
     */
    public void update(TimeUnit timeUnit);
}
