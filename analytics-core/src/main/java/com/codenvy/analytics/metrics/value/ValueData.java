/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
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
