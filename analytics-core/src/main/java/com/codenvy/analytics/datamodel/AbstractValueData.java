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


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractValueData implements ValueData {

    private Integer hash;

    @Override
    public ValueData add(ValueData valueData) {
        if (getClass() != valueData.getClass()) {
            throw new IllegalArgumentException(
                    "Can not add two different classes " + getClass().getName() + " and " + valueData.getClass().getName());
        }

        return doAdd(valueData);
    }

    @Override
    public ValueData subtract(ValueData valueData) {
        if (getClass() != valueData.getClass()) {
            throw new IllegalArgumentException(
                    "Can not subtract two different classes " + getClass().getName() + " and " + valueData.getClass().getName());
        }

        return doSubtract(valueData);
    }

    @Override
    public String toString() {
        return getAsString();
    }

    @Override
    public boolean equals(Object object) {
        return getClass() == object.getClass() && doEquals((ValueData)object);
    }


    @Override
    public int hashCode() {
        if (hash == null) {
            hash = doHashCode();
        }

        return hash;
    }

    /** @see #equals(Object) */
    abstract protected boolean doEquals(ValueData valueData);

    /** @see #hashCode() */
    abstract protected int doHashCode();

    /** Combines two value data into one new single instance */
    abstract protected ValueData doAdd(ValueData valueData);

    /** Subtract passed value data from the current one */
    abstract protected ValueData doSubtract(ValueData valueData);

}
