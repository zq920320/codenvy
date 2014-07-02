/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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

package com.codenvy.analytics.datamodel;

import java.io.Externalizable;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public interface ValueData extends Externalizable {

    /**
     * Combines two {@link com.codenvy.analytics.datamodel.ValueData} into one single. The passed and the current
     * {@link com.codenvy.analytics.datamodel.ValueData} won't be modified.
     */
    ValueData add(ValueData valueData);

    /**
     * Subtracts passed {@link com.codenvy.analytics.datamodel.ValueData} from the current one. The passed and the current
     * {@link com.codenvy.analytics.datamodel.ValueData} won't be modified.
     */
    ValueData subtract(ValueData valueData);

    /** Represents {@link com.codenvy.analytics.datamodel.ValueData} as {@link String}. */
    String getAsString();

    /** @return type of value as {@link String} */
    String getType();
}
