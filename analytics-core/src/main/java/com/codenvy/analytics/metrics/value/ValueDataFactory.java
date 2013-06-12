/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import com.codenvy.analytics.scripts.executor.ScriptExecutor;

import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
     * Instantiates empty {@link ValueData}.
     */
    public static ValueData createEmptyValueData(Class< ? extends ValueData> clazz) throws IOException {
        if (clazz == ListStringValueData.class) {
            return ListStringValueData.EMPTY;
        }

        throw new IOException("Unknown class " + clazz.getName());
    }

    /**
     * Instantiates {@link ValueData} from result obtained by {@link ScriptExecutor}.
     */
    public static ValueData createValueData(Class< ? > clazz, Iterator<Tuple> iter) throws IOException {
        if (clazz == LongValueData.class) {
            Tuple tuple = ensureSingleResult(iter);
            return createLongValueData(tuple);

        } else if (clazz == DoubleValueData.class) {
            Tuple tuple = ensureSingleResult(iter);
            return createDoubleValueData(tuple);
        }
        else if (clazz == ListStringValueData.class) {
            return createListStringValueData(iter);
        }

        else if (clazz == ListListStringValueData.class) {
            return createListListStringValueData(iter);
        }

        else if (clazz == MapStringLongValueData.class) {
            return createMapStringLongValueData(iter);
        }

        else if (clazz == MapStringListValueData.class) {
            return createMapStringListValueData(iter);
        }


        throw new IOException("Unknown class " + clazz.getName());
    }

    private static ValueData createMapStringListValueData(Iterator<Tuple> iter) throws IOException {
        if (!iter.hasNext()) {
            return new MapStringListValueData(Collections.<String, ListStringValueData> emptyMap());
        }

        Map<String, ListStringValueData> result = new HashMap<String, ListStringValueData>();
        while (iter.hasNext()) {
            Tuple tuple = iter.next();

            validateTupleSize(tuple, 2);

            String key = tuple.get(0).toString();
            ListStringValueData value = createListStringValueData(((DataBag)tuple.get(1)).iterator());

            result.put(key, value);
        }

        return new MapStringListValueData(result);
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

    private static ValueData createLongValueData(Tuple tuple) throws IOException {
        Long value = tuple == null ? 0L : (Long)tuple.get(0);
        return new LongValueData(value);
    }

    private static ValueData createDoubleValueData(Tuple tuple) throws IOException {
        Double value = tuple == null ? 0D : (Double)tuple.get(0);
        return new DoubleValueData(value);
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
