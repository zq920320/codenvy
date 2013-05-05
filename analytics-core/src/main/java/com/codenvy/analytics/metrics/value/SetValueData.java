/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class SetValueData<T extends ValueData> extends AbstractValueData {

    protected final Set<T> value;

    public SetValueData(String value) {
        this.value = parse(value);
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

    protected Set<T> parse(String line) {
        line = line.substring(1, line.length() - 1); // removes '[' and ']'

        if (line.equals(EMPTY_VALUE)) {
            return Collections.<T> emptySet();
        }

        if (isComplexStructure(line)) {
            return parseComplexStructure(line);
        } else {
            return parseSimpleStructure(line);
        }
    }

    private Set<T> parseSimpleStructure(String line) {
        String[] splittedLine = line.split(ITEM_DELIMITER);

        Set<T> result = new HashSet<T>(splittedLine.length);
        for (String str : splittedLine) {
            result.add(createInnerValueData(str.trim()));
        }

        return result;
    }

    private Set<T> parseComplexStructure(String line) {
        Set<T> result = new HashSet<T>();

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

    /**
     * Return the union of two sets.
     */
    @SuppressWarnings("unchecked")
    protected Set<T> unionInternalValues(ValueData valueData) {
        SetValueData<T> addValueData = (SetValueData<T>)valueData;

        Set<T> newValue = new HashSet<T>(this.value.size() + addValueData.value.size());
        newValue.addAll(this.value);
        newValue.addAll(addValueData.value);

        return newValue;
    }

    /**
     * {@inheritDoc}
     */
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
}
