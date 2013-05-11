/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.pig.data.Tuple;

import com.codenvy.analytics.scripts.executor.ScriptExecutor;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ValueDataFactory {


    /**
     * Instantiates {@link ValueData} from raw string.
     */
    public static ValueData createValueData(Class< ? > clazz, String value) throws NoSuchMethodException,
                                                                           SecurityException,
                                                                           InstantiationException,
                                                                           IllegalAccessException,
                                                                           IllegalArgumentException,
                                                                           InvocationTargetException {

        Constructor< ? > constructor = clazz.getConstructor(String.class);
        return (ValueData)constructor.newInstance(value);
    }

    /**
     * Instantiates {@link ValueData} from result obtained by {@link ScriptExecutor}.
     */
    public static ValueData createValueData(Class< ? > clazz, Iterator<Tuple> iter) throws IOException {
        if (clazz == StringValueData.class) {
            Tuple tuple = ensureSingleResult(iter);
            return createStringValueData(tuple);

        } else if (clazz == LongValueData.class) {
            Tuple tuple = ensureSingleResult(iter);
            return createLongValueData(tuple);

        } else if (clazz == DoubleValueData.class) {
            Tuple tuple = ensureSingleResult(iter);
            return createDoubleValueData(tuple);
        }

        else if (clazz == SetStringValueData.class) {
            return createSetStringValueData(iter);
        }

        else if (clazz == ListStringValueData.class) {
            return createListStringValueData(iter);
        }

        else if (clazz == MapStringLongValueData.class) {
            return createMapStringLongValueData(iter);
        }

        else if (clazz == SetListStringValueData.class) {
            return createSetListStringValueData(iter);
        }

        else if (clazz == ListDoubleValueData.class) {
            return createListDoubleValueData(iter);
        }

        else if (clazz == ListListStringValueData.class) {
            return createListListStringValueData(iter);
        }

        throw new IOException("Unknown class " + clazz.getName());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ListListStringValueData createListListStringValueData(Iterator<Tuple> iter) throws IOException {
        if (!iter.hasNext()) {
            return new ListListStringValueData(Collections.<ListStringValueData> emptyList());
        }

        List<ListStringValueData> result = new ArrayList<ListStringValueData>();
        while (iter.hasNext()) {
            Tuple tuple = iter.next();

            validateTupleSize(tuple, 1);

            Iterator iterator = ((Tuple)tuple.get(0)).iterator();
            result.add(createListStringValueData(iterator));
        }

        return new ListListStringValueData(result);
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ValueData createSetListStringValueData(Iterator<Tuple> iter) throws IOException {
        if (!iter.hasNext()) {
            return new SetListStringValueData(Collections.<ListStringValueData> emptySet());
        }

        Set<ListStringValueData> result = new HashSet<ListStringValueData>();
        while (iter.hasNext()) {
            Tuple tuple = iter.next();

            validateTupleSize(tuple, 1);

            Iterator iterator = ((Tuple)tuple.get(0)).iterator();
            result.add(createListStringValueData(iterator));
        }

        return new SetListStringValueData(result);
    }

    private static ListDoubleValueData createListDoubleValueData(Iterator<Tuple> iter) throws IOException {
        if (!iter.hasNext()) {
            return new ListDoubleValueData(Collections.<Double> emptyList());
        }

        List<Double> result = new ArrayList<Double>();
        while (iter.hasNext()) {
            Tuple tuple = iter.next();

            validateTupleSize(tuple, 1);
            result.add((Double)tuple.get(0));
        }

        return new ListDoubleValueData(result);
    }

    private static ListStringValueData createListStringValueData(Iterator<Tuple> iter) throws IOException {
        if (!iter.hasNext()) {
            return new ListStringValueData(Collections.<String> emptyList());
        }

        List<String> result = new ArrayList<String>();
        while (iter.hasNext()) {
            Tuple tuple = iter.next();

            validateTupleSize(tuple, 1);
            result.add(tuple.get(0).toString());
        }

        return new ListStringValueData(result);
    }

    private static ValueData createMapStringLongValueData(Iterator<Tuple> iter) throws IOException {
        if (!iter.hasNext()) {
            return new MapStringLongValueData(Collections.<String, Long> emptyMap());
        }

        Map<String, Long> result = new HashMap<String, Long>();
        while (iter.hasNext()) {
            Tuple tuple = iter.next();

            validateTupleSize(tuple, 2);

            String key = tuple.get(0).toString();
            Long value = (Long)tuple.get(1);

            result.put(key, value);
        }

        return new MapStringLongValueData(result);
    }

    private static ValueData createSetStringValueData(Iterator<Tuple> iter) throws IOException {
        if (!iter.hasNext()) {
            return new SetStringValueData(Collections.<String> emptySet());
        }

        Set<String> result = new HashSet<String>();
        while (iter.hasNext()) {
            Tuple tuple = iter.next();

            validateTupleSize(tuple, 1);
            result.add(tuple.get(0).toString());
        }

        return new SetStringValueData(result);
    }

    private static ValueData createLongValueData(Tuple tuple) throws IOException {
        Long value = tuple == null ? 0L : (Long)tuple.get(0);
        return new LongValueData(value);
    }

    private static ValueData createDoubleValueData(Tuple tuple) throws IOException {
        Double value = tuple == null ? 0D : (Double)tuple.get(0);
        return new DoubleValueData(value);
    }

    private static ValueData createStringValueData(Tuple tuple) throws IOException {
        String value = tuple == null ? "" : tuple.get(0).toString();
        return new StringValueData(value);
    }

    private static Tuple ensureSingleResult(Iterator<Tuple> iter) throws IOException {
        Tuple tuple = iter.hasNext() ? iter.next() : null;

        if (iter.hasNext()) {
            throw new IOException("The result has wrong format: returned more than one tuples.");
        } else if (tuple != null) {
            validateTupleSize(tuple, 1);
        }

        return tuple;
    }

    private static void validateTupleSize(Tuple tuple, int size) throws IOException {
        if (tuple.size() != size) {
            throw new IOException("The result has wrong format: the tuple contains more than " + size + " object(s)");
        }
    }
}
