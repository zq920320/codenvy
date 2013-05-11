/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.io.Externalizable;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public interface ValueData extends Externalizable {

    /**
     * Unions two {@link ValueData} into one single. The passed and current {@link ValueData} will not be modified.
     */
    ValueData union(ValueData valueData);

    /**
     * Represents {@link ValueData} as {@link String}.
     */
    String getAsString();

    /**
     * @return value as {@link Long}
     */
    long getAsLong();

    /**
     * @return value as {@link Double}
     */
    double getAsDouble();
}
