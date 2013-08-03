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
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class SetValueData<T> extends AbstractValueData {

    private final Set<T> value;

    public SetValueData(Collection<T> value) {
        this.value = new HashSet<>(value.size());
        this.value.addAll(value);
    }

    public Set<T> getAll() {
        return Collections.unmodifiableSet(value);
    }

    public int size() {
        return value.size();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected ValueData doUnion(ValueData valueData) {
        Set<T> value1 = this.value;
        Set<T> value2 = ((SetValueData<T>)valueData).getAll();

        Set<T> newValue = new HashSet<>();
        newValue.addAll(value1);
        newValue.addAll(value2);

        return createInstance(newValue);
    }

    /** {@inheritDoc} */
    @Override
    public void writeTo(ObjectOutputStream out) throws IOException {
        out.writeInt(value.size());
        for (T item : value) {
            writeItem(out, item);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getAsString() {
        StringBuilder builder = new StringBuilder();

        for (T valueData : value) {
            if (builder.length() != 0) {
                builder.append(',');
            }

            builder.append(' ');
            builder.append(valueData.toString());
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
    protected boolean doEquals(Object object) {
        SetValueData<?> valueData = (SetValueData<?>)object;
        return this.value.size() == valueData.value.size() && this.value.containsAll(valueData.value);
    }


    /** {@inheritDoc} */
    @Override
    protected int doHashCode() {
        int hash = 0;

        for (T t : value) {
            hash += t.hashCode();
        }

        return hash;
    }

    abstract protected ValueData createInstance(Set<T> value);

    abstract protected void writeItem(ObjectOutputStream out, T item) throws IOException;
}
