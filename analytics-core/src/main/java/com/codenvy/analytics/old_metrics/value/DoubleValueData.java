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


package com.codenvy.analytics.old_metrics.value;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class DoubleValueData extends AbstractValueData {

    public static final DoubleValueData DEFAULT = new DoubleValueData(Double.valueOf("0"));

    private final double value;

    public DoubleValueData(ObjectInputStream in) throws IOException {
        value = readFrom(in);
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
    protected DoubleValueData doUnion(ValueData valueData) {
        return new DoubleValueData(value + valueData.getAsDouble());
    }

    /** {@inheritDoc} */
    @Override
    public double getAsDouble() {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public void writeTo(ObjectOutputStream out) throws IOException {
        out.writeDouble(value);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doEquals(Object object) {
        return value == ((DoubleValueData)object).value;
    }

    /** {@inheritDoc} */
    @Override
    public int doHashCode() {
        return (int)value;
    }

    /** Deserialization. */
    private double readFrom(ObjectInputStream in) throws IOException {
        return in.readDouble();
    }
}