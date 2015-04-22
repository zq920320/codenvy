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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsSet;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class SetValueData extends CollectionValueData {

    private Set<ValueData> value;

    public static final SetValueData DEFAULT = new SetValueData(Collections.<ValueData>emptySet());

    public SetValueData() {
    }

    public SetValueData(Collection<ValueData> value) {
        this.value = new HashSet<>(value);
    }

    public Set<ValueData> getAll() {
        return Collections.unmodifiableSet(value);
    }

    public int size() {
        return value.size();
    }

    @Override
    protected ValueData doSubtract(ValueData valueData) {
        Set<ValueData> result = new HashSet<>(value);
        result.removeAll(treatAsSet(valueData));
        return new SetValueData(result);
    }

    @Override
    protected ValueData doAdd(ValueData valueData) {
        Set<ValueData> result = new HashSet<>(value);
        result.addAll(treatAsSet(valueData));
        return new SetValueData(result);
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
        return ValueDataTypes.SET.toString();
    }

    @Override
    protected boolean doEquals(ValueData valueData) {
        return this.value.equals(((SetValueData)valueData).value);
    }

    @Override
    protected int doHashCode() {
        return value.hashCode();
    }

}
