/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class SetValueData<T> extends AbstractValueData {

    protected Set<T> value;

    public SetValueData() {
    }

    public SetValueData(Collection<T> value) {
        this.value = new HashSet<T>(value.size());
        this.value.addAll(value);
    }

    /**
     * @return unmodifiable collection
     */
    public Set<T> getAll() {
        return Collections.unmodifiableSet(value);
    }

    /**
     * {@inheritDoc}
     */
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

    /** {@inheritedDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected ValueData doUnion(ValueData valueData) {
        SetValueData<T> addVD = (SetValueData<T>)valueData;

        Set<T> result = new HashSet<T>(this.value);
        result.addAll(addVD.value);

        return createInstance(result);
    }

    /** {@inheritedDoc} */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(value.size());
        for (T item : value) {
            writeItem(out, item);
        }
    }

    /** {@inheritedDoc} */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int size = in.readInt();

        value = new HashSet<T>(size);
        for (int i = 0; i < size; i++) {
            value.add(readItem(in));
        }
    }

    /** {@inheritedDoc} */
    @Override
    protected boolean doEquals(Object object) {
        SetValueData< ? > valueData = (SetValueData< ? >)object;
        return this.value.size() == valueData.value.size() && this.value.containsAll(valueData.value);
    }

    /**
     * Ensures the different item order will provide the same result.<br>
     * {@inheritDoc}
     */
    @Override
    public int doHashCode() {
        int hash = 0;
        for (T t : value) {
            hash += t.hashCode();
        }

        return hash;
    }

    protected abstract void writeItem(ObjectOutput out, T item) throws IOException;

    protected abstract T readItem(ObjectInput in) throws IOException;

    protected abstract ValueData createInstance(Collection<T> value);
}
