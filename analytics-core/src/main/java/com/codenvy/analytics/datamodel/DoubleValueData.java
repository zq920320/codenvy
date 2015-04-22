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
package com.codenvy.analytics.datamodel;

import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsDouble;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class DoubleValueData extends NumericValueData {

    public static final DoubleValueData DEFAULT = new DoubleValueData(0);

    private double value;

    /** For serialization one. */
    public DoubleValueData() {
    }

    public DoubleValueData(double value) {
        this.value = value;
    }

    public static ValueData valueOf(Double value) {
        return new DoubleValueData(value);
    }

    @Override
    public String getAsString() {
        return Double.toString(value);
    }

    @Override
    public String getType() {
        return ValueDataTypes.DOUBLE.toString();
    }

    @Override
    protected boolean doEquals(ValueData valueData) {
        return value == ((DoubleValueData)valueData).value;
    }

    @Override
    protected int doHashCode() {
        return (int)Double.doubleToLongBits(value);
    }

    @Override
    protected ValueData doAdd(ValueData valueData) {
        return new DoubleValueData(value + treatAsDouble(valueData));
    }

    @Override
    protected ValueData doSubtract(ValueData valueData) {
        return new DoubleValueData(value - treatAsDouble(valueData));
    }

    public double getAsDouble() {
        return value;
    }

}
