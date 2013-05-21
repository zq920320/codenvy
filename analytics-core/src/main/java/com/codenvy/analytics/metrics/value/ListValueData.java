/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class ListValueData<T> extends AbstractValueData {

    private List<T> value;

    public ListValueData() {
    }

    public ListValueData(Collection<T> value) {
        this.value = new ArrayList<T>(value.size());
        this.value.addAll(value);
    }

    public List<T> getAll() {
        return Collections.unmodifiableList(value);
    }

    public int size() {
        return value.size();
    }

    /** {@inheritedDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected ValueData doUnion(ValueData valueData) {
        List<T> value1 = this.value;
        List<T> value2 = ((ListValueData<T>)valueData).getAll();

        List<T> newValue = new ArrayList<T>(value1.size() + value2.size());
        newValue.addAll(value1);
        newValue.addAll(value2);

        return createInstance(newValue);
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

        value = new ArrayList<T>(size);
        for (int i = 0; i < size; i++) {
            value.add(readItem(in));
        }
    }

    /** {@inheritedDoc} */
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doEquals(Object object) {
        ListValueData< ? > valueData = (ListValueData< ? >)object;

        if (this.value.size() != valueData.value.size()) {
            return false;
        }
        
        for (int i = 0; i < this.value.size(); i++) {
            if (!this.value.get(i).equals(valueData.value.get(i))) {
                return false;
            }
        }

        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected int doHashCode() {
        int hash = 0;

        for (T t : value) {
            hash = hash * 31 + t.hashCode();
        }

        return hash;
    }

    abstract protected ValueData createInstance(List<T> value);

    abstract protected void writeItem(ObjectOutput out, T item) throws IOException;

    abstract protected T readItem(ObjectInput in) throws IOException;
}
