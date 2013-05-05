/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class ListValueData<T extends ValueData> extends AbstractValueData {

    private final List<T> value;

    public ListValueData(String value) {
        this.value = parse(value);
    }

    public ListValueData(Collection<T> value) {
        this.value = new ArrayList<T>(value.size());
        this.value.addAll(value);
    }

    public List<T> getAll() {
        return Collections.unmodifiableList(value);
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

        return createValueData(newValue);
    }
    
    /** {@inheritedDoc} */
    @Override
    public String getAsString() {
        StringBuilder builder = new StringBuilder();

        for (ValueData valueData : value) {
            if (builder.length() != 0) {
                builder.append(ITEM_DELIMITER);
            }

            builder.append(' ');
            builder.append(valueData.getAsString());
        }

        if (builder.length() != 0) {
            builder.setCharAt(0, '[');
            builder.append(']');
        } else {
            builder.append('[');
            builder.append(EMPTY_VALUE);
            builder.append(']');
        }

        return builder.toString();
    }

    protected List<T> parse(String line) {
        line = line.substring(1, line.length() - 1); // removes '[' and ']'

        if (line.equals(EMPTY_VALUE)) {
            return Collections.<T> emptyList();
        }

        if (isComplexStructure(line)) {
            return parseComplexStructure(line);
        } else {
            return parseSimpleStructure(line);
        }
    }

    private List<T> parseSimpleStructure(String line) {
        String[] splittedLine = line.split(ITEM_DELIMITER);

        List<T> result = new ArrayList<T>(splittedLine.length);
        for (String str : splittedLine) {
            result.add(createInnerValueData(str.trim()));
        }

        return result;
    }

    private List<T> parseComplexStructure(String line) {
        List<T> result = new ArrayList<T>();

        int beginIndex;
        while ((beginIndex = line.indexOf("[")) >= 0) {
            int endIndex = line.indexOf("]");

            result.add(createInnerValueData(line.substring(beginIndex, endIndex + 1)));
            line = line.substring(endIndex + 1);
        }

        return result;
    }

    private boolean isComplexStructure(String line) {
        return line.contains("[");
    }

    /** @return T instance */
    abstract protected T createInnerValueData(String str);

    /** Factory method */
    abstract protected ValueData createValueData(List<T> value);

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
}
