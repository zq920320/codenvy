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


package com.codenvy.analytics.datamodel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class DoubleValueData extends AbstractValueData {

    public static final DoubleValueData DEFAULT = new DoubleValueData(0);

    private double value;

    /** For serialization one. */
    public DoubleValueData() {
    }

    public DoubleValueData(double value) {
        this.value = value;
    }

    /** {@inheritDoc} */
    @Override
    public String getAsString() {
        return Double.toString(value);
    }

    /** {@inheritDoc} */
    @Override
    public String getType() {
        return ValueDataTypes.DOUBLE.toString();
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doEquals(ValueData valueData) {
        return value == ((DoubleValueData)valueData).value;
    }

    /** {@inheritDoc} */
    @Override
    protected int doHashCode() {
        return (int)Double.doubleToLongBits(value);
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData doUnion(ValueData valueData) {
        return new DoubleValueData(value + ((DoubleValueData)valueData).value);
    }

    /** {@inheritDoc} */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeDouble(value);
    }

    /** {@inheritDoc} */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readDouble();
    }

    public double getAsDouble() {
        return value;
    }
}
