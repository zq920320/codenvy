/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * Is thrown when cumulative metric can be evaluated since there is no initial value.
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
@SuppressWarnings("serial")
public class InitialValueNotFoundException extends IOException {

    public InitialValueNotFoundException(String message) {
        super(message);
    }
}
