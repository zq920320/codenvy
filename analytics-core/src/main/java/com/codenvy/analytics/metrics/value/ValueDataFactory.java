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

import com.codenvy.analytics.scripts.executor.ScriptExecutor;

import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ValueDataFactory {


    /** Instantiates {@link ValueData} from raw string. */
    public static ValueData createValueData(Class<?> clazz, String value) throws NoSuchMethodException,
                                                                                 SecurityException,
                                                                                 InstantiationException,
                                                                                 IllegalAccessException,
                                                                                 IllegalArgumentException,
                                                                                 InvocationTargetException {

        Constructor<?> constructor = clazz.getConstructor(String.class);
        return (ValueData)constructor.newInstance(value);
    }

    /** Instantiates empty {@link ValueData}. */
    public static ValueData createEmptyValueData(Class<? extends ValueData> clazz) throws IOException {
        if (clazz == ListStringValueData.class) {
            return ListStringValueData.EMPTY;
        } else if (clazz == ListListStringValueData.class) {
            return ListListStringValueData.EMPTY;
        } else if (clazz == LongValueData.class) {
            return LongValueData.EMPTY;
        }

        throw new IOException("Unknown class " + clazz.getName());
    }

    /** Instantiates {@link ValueData} from result obtained by {@link ScriptExecutor}. */
    public static ValueData createValueData(Class<?> clazz, Iterator<Tuple> iter) throws IOException {
        if (clazz == LongValueData.class) {
            Tuple tuple = ensureSingleResult(iter);
            return createLongValueData(tuple);

        } else if (clazz == DoubleValueData.class) {
            Tuple tuple = ensureSingleResult(iter);
            return createDoubleValueData(tuple);
        } else if (clazz == ListStringValueData.class) {
            return createListStringValueData(iter);
        } else if (clazz == ListListStringValueData.class) {
            return createListListStringValueData(iter);
        } else if (clazz == MapStringLongValueData.class) {
            return createMapStringLongValueData(iter);
        } else if (clazz == MapStringListListStringValueData.class) {
            return createMapStringListValueData(iter);
        }


        throw new IOException("Unknown class " + clazz.getName());
    }

    private static ValueData createMapStringListValueData(Iterator<Tuple> iter) throws IOException {
        if (!iter.hasNext()) {
            return new MapStringListListStringValueData(Collections.<String, ListListStringValueData>emptyMap());
        }

        Map<String, ListListStringValueData> result = new HashMap<String, ListListStringValueData>();
        while (iter.hasNext()) {
            Tuple tuple = iter.next();

            validateTupleSize(tuple, 2);

            String key = tuple.get(0).toString();
            ListListStringValueData value = createListListStringValueData(((DataBag)tuple.get(1)).iterator());

            result.put(key, value);
        }

        return new MapStringListListStringValueData(result);
    }


    private static ValueData createMapStringLongValueData(Iterator<Tuple> iter) throws IOException {
        if (!iter.hasNext()) {
            return new MapStringLongValueData(Collections.<String, Long>emptyMap());
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
            return new ListListStringValueData(Collections.<ListStringValueData>emptyList());
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
            return new ListStringValueData(Collections.<String>emptyList());
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
