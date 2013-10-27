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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ListValueData extends AbstractValueData {

    public static final ListValueData DEFAULT = new ListValueData(Collections.<RowValueData>emptyList());

    private List<RowValueData> value;

    public ListValueData() {
    }

    public ListValueData(Collection<RowValueData> value) {
        this.value = new ArrayList<>(value);
    }

    public List<RowValueData> getAll() {
        return Collections.unmodifiableList(value);
    }

    public int size() {
        return value.size();
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData doUnion(ValueData valueData) {
        ListValueData object = (ListValueData)valueData;

        List<RowValueData> result = new ArrayList<>(this.value.size() + object.size());
        result.addAll(this.value);
        result.addAll(object.value);

        return new ListValueData(result);
    }

    /** {@inheritDoc} */
    @Override
    public String getAsString() {
        StringBuilder builder = new StringBuilder();

        for (RowValueData valueData : value) {
            if (builder.length() != 0) {
                builder.append(',');
            }

            builder.append(' ');
            builder.append(valueData.getAsString());
        }

        if (builder.length() != 0) {
            builder.setCharAt(0, '[');
            builder.append(']');
        } else {
            builder.append("[]");
        }

        return builder.toString();
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doEquals(ValueData valueData) {
        return this.value.equals(((ListValueData)valueData).value);
    }

    /** {@inheritDoc} */
    @Override
    protected int doHashCode() {
        return value.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(value.size());
        for (RowValueData item : value) {
            out.writeObject(item);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int size = in.readInt();

        value = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            value.add((RowValueData)in.readObject());
        }
    }
}
