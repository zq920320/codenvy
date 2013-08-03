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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class LongValueData extends AbstractValueData {

    public static final LongValueData DEFAULT = new LongValueData(0);
    private static final long serialVersionUID = 1L;

    private long value;

    public LongValueData() {
    }

    public LongValueData(String value) {
        this.value = Long.valueOf(value);
    }

    public LongValueData(long value) {
        this.value = value;
    }

    /** {@inheritDoc} */
    @Override
    public String getAsString() {
        return Long.toString(value);
    }

    /** {@inheritDoc} */
    @Override
    protected LongValueData doUnion(ValueData valueData) {
        return new LongValueData(value + valueData.getAsLong());
    }

    /** {@inheritDoc} */
    @Override
    public long getAsLong() {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public double getAsDouble() {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doEquals(Object object) {
        return value == ((LongValueData)object).value;
    }

    /** {@inheritDoc} */
    @Override
    public int doHashCode() {
        return (int)value;
    }

    /** {@inheritedDoc} */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(value);

    }

    /** {@inheritedDoc} */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readLong();
    }
}
