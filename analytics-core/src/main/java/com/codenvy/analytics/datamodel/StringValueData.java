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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class StringValueData extends AbstractValueData {

    public static final StringValueData DEFAULT = new StringValueData("");

    private String value;

    /** For serialization one. */
    public StringValueData() {
    }

    public StringValueData(String value) {
        this.value = value;
    }

    @Override
    public String getAsString() {
        return value;
    }

    @Override
    public String getType() {
        return ValueDataTypes.STRING.toString();
    }

    public static StringValueData valueOf(String value) {
        return new StringValueData(value);
    }

    @Override
    protected boolean doEquals(ValueData valueData) {
        return value.equals(((StringValueData)valueData).value);
    }

    @Override
    protected int doHashCode() {
        return value.hashCode();
    }

    @Override
    protected ValueData doSubtract(ValueData valueData) {
        if (value.equals(valueData.getAsString())) {
            return DEFAULT;
        } else if (value.endsWith("\n" + valueData.getAsString())) {
            int endIndex = value.length() - valueData.getAsString().length() - 1;
            return new StringValueData(value.substring(0, endIndex));
        } else {
            return new StringValueData(value);
        }
    }

    @Override
    protected ValueData doAdd(ValueData valueData) {
        return new StringValueData(value + "\n" + valueData.getAsString());
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(value);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readUTF();
    }
}
