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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ListValueData extends CollectionValueData {

    private List<ValueData> value;

    public static final ListValueData DEFAULT = new ListValueData(Collections.<ValueData>emptyList());

    public ListValueData() {
    }

    public ListValueData(List<ValueData> value) {
        this.value = new ArrayList<>(value);
    }

    public List<ValueData> getAll() {
        return Collections.unmodifiableList(value);
    }

    public int size() {
        return value.size();
    }

    @Override
    protected ValueData doUnion(ValueData valueData) {
        ListValueData object = (ListValueData)valueData;

        List<ValueData> result = new ArrayList<>(this.size() + object.size());
        result.addAll(this.value);
        result.addAll(object.value);

        return new ListValueData(result);
    }
    
    /**
     * @return <current list> - <subtrahend list>
     */
    public ListValueData doSubtract(ListValueData subtrahendData) {
        List<String> subtrahenValues = new ArrayList<>(subtrahendData.size());

        for (ValueData item: subtrahendData.value) {
            subtrahenValues.add(item.getAsString());
        }  

        List<ValueData> result = new ArrayList<>(this.size());
        for (ValueData item: this.value) {
            if (!subtrahenValues.contains(item.getAsString())) {
                result.add(item);
            }
        }

        return new ListValueData(result);
    }
    
    public ListValueData subList(int fromIndex, int toIndex) {
        List<ValueData> result = getAll().subList(fromIndex, toIndex);
        
        return new ListValueData(result);
    }

    @Override
    public String getAsString() {
        StringBuilder builder = new StringBuilder();

        for (ValueData valueData : value) {
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

    @Override
    public String getType() {
        return ValueDataTypes.LIST.toString();
    }

    @Override
    protected boolean doEquals(ValueData valueData) {
        return this.value.equals(((ListValueData)valueData).value);
    }

    @Override
    protected int doHashCode() {
        return value.hashCode();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(value.size());
        for (ValueData item : value) {
            out.writeObject(item);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int size = in.readInt();

        value = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            value.add((ValueData)in.readObject());
        }
    }
}
