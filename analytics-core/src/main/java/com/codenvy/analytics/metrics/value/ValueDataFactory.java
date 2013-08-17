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
import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ValueDataFactory {

    /** Instantiates default {@link ValueData}. */
    public static ValueData createDefaultValue(Class<? extends ValueData> clazz) throws IOException {
        if (clazz == LongValueData.class) {
            return LongValueData.DEFAULT;

        } else if (clazz == DoubleValueData.class) {
            return DoubleValueData.DEFAULT;

        } else if (clazz == ListStringValueData.class) {
            return ListStringValueData.DEFAULT;

        } else if (clazz == SetStringValueData.class) {
            return SetStringValueData.DEFAULT;

        } else if (clazz == ListListStringValueData.class) {
            return ListListStringValueData.DEFAULT;

        } else if (clazz == MapStringLongValueData.class) {
            return MapStringLongValueData.DEFAULT;

        } else if (clazz == MapStringListListStringValueData.class) {
            return MapStringListListStringValueData.DEFAULT;

        } else if (clazz == MapStringSetValueData.class) {
            return MapStringSetValueData.DEFAULT;

        } else if (clazz == MapStringListValueData.class) {
            return MapStringListValueData.DEFAULT;

        } else if (clazz == MapStringFixedLongListValueData.class) {
            return MapStringFixedLongListValueData.DEFAULT;

        } else if (clazz == MapListLongValueData.class) {
            return MapListLongValueData.DEFAULT;
        } else if (clazz == FixedListLongValueData.class) {
            return FixedListLongValueData.DEFAULT;
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

        } else if (clazz == FixedListLongValueData.class) {
            return createFixedListLongValueData(iter);

        } else if (clazz == SetStringValueData.class) {
            return createSetStringValueData(iter);

        } else if (clazz == ListListStringValueData.class) {
            return createListListStringValueData(iter);

        } else if (clazz == MapStringLongValueData.class) {
            return createMapStringLongValueData(iter);

        } else if (clazz == MapStringListListStringValueData.class) {
            return createMapStringListListValueData(iter);

        } else if (clazz == MapStringSetValueData.class) {
            return createMapStringSetValueData(iter);

        } else if (clazz == MapStringListValueData.class) {
            return createMapStringListValueData(iter);

        } else if (clazz == MapStringFixedLongListValueData.class) {
            return createMapStringFixedLongListValueData(iter);


        } else if (clazz == MapListLongValueData.class) {
            return createMapListLongValueData(iter);
        }

        throw new IOException("Unknown class " + clazz.getName());
    }

    public static ValueData createValueData(Object value) {
        if (value instanceof ValueData) {
            return (ValueData)value;

        } else if (value instanceof Long) {
            return new LongValueData((Long)value);

        } else if (value instanceof String) {
            return new StringValueData((String)value);

        } else if (value instanceof Double) {
            return new DoubleValueData((Double)value);

        } else {
            throw new IllegalStateException("Unknown class" + value.getClass().getName());
        }
    }

    private static ValueData createMapListLongValueData(Iterator<Tuple> iter) throws IOException {
        if (!iter.hasNext()) {
            return new MapListLongValueData(Collections.<ListStringValueData, Long>emptyMap());
        }

        Map<ListStringValueData, Long> result = new HashMap<>();
        while (iter.hasNext()) {
            Tuple tuple = iter.next();

            validateTupleSize(tuple, 2);

            ListStringValueData key = createListStringValueData(((DataBag)tuple.get(0)).iterator());
            Long value = (Long)tuple.get(1);

            result.put(key, value);
        }

        return new MapListLongValueData(result);
    }

    private static ValueData createMapStringListValueData(Iterator<Tuple> iter) throws IOException {
        if (!iter.hasNext()) {
            return new MapStringListValueData(Collections.<String, ListStringValueData>emptyMap());
        }

        Map<String, ListStringValueData> result = new HashMap<>();
        while (iter.hasNext()) {
            Tuple tuple = iter.next();

            validateTupleSize(tuple, 2);

            String key = tuple.get(0).toString();
            ListStringValueData value = createListStringValueData(((DataBag)tuple.get(1)).iterator());

            result.put(key, value);
        }

        return new MapStringListValueData(result);
    }

    private static ValueData createMapStringFixedLongListValueData(Iterator<Tuple> iter) throws IOException {
        if (!iter.hasNext()) {
            return new MapStringFixedLongListValueData(Collections.<String, FixedListLongValueData>emptyMap());
        }

        Map<String, FixedListLongValueData> result = new HashMap<>();
        while (iter.hasNext()) {
            Tuple tuple = iter.next();

            validateTupleSize(tuple, 2);

            String key = tuple.get(0).toString();
            FixedListLongValueData value = createFixedListLongValueData(((DataBag)tuple.get(1)).iterator());

            result.put(key, value);
        }

        return new MapStringFixedLongListValueData(result);
    }

    private static ValueData createMapStringSetValueData(Iterator<Tuple> iter) throws IOException {
        if (!iter.hasNext()) {
            return new MapStringSetValueData(Collections.<String, SetStringValueData>emptyMap());
        }

        Map<String, SetStringValueData> result = new HashMap<>();
        while (iter.hasNext()) {
            Tuple tuple = iter.next();

            validateTupleSize(tuple, 2);

            String key = tuple.get(0).toString();
            SetStringValueData value = createSetStringValueData(((DataBag)tuple.get(1)).iterator());

            result.put(key, value);
        }

        return new MapStringSetValueData(result);
    }

    private static ValueData createMapStringListListValueData(Iterator<Tuple> iter) throws IOException {
        if (!iter.hasNext()) {
            return new MapStringListListStringValueData(Collections.<String, ListListStringValueData>emptyMap());
        }

        Map<String, ListListStringValueData> result = new HashMap<>();
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
        if (!iter.hasNext()) return new MapStringLongValueData(Collections.<String, Long>emptyMap());

        Map<String, Long> result = new HashMap<>();
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

        List<ListStringValueData> result = new ArrayList<>();
        while (iter.hasNext()) {
            Tuple tuple = iter.next();

            validateTupleSize(tuple, 1);

            Iterator iterator = ((Tuple)tuple.get(0)).iterator();
            result.add(createListStringValueData(iterator));
        }

        return new ListListStringValueData(result);
    }

    private static FixedListLongValueData createFixedListLongValueData(Iterator<Tuple> iter) throws IOException {
        if (!iter.hasNext()) return new FixedListLongValueData(Collections.<Long>emptyList());

        List<Long> result = new ArrayList<>();
        while (iter.hasNext()) {
            Tuple tuple = iter.next();

            validateTupleSize(tuple, 1);
            result.add((Long)tuple.get(0));
        }

        return new FixedListLongValueData(result);
    }

    private static ListStringValueData createListStringValueData(Iterator<Tuple> iter) throws IOException {
        if (!iter.hasNext()) return new ListStringValueData(Collections.<String>emptyList());

        List<String> result = new ArrayList<>();
        while (iter.hasNext()) {
            Tuple tuple = iter.next();

            validateTupleSize(tuple, 1);
            result.add(tuple.get(0).toString());
        }

        return new ListStringValueData(result);
    }

    private static SetStringValueData createSetStringValueData(Iterator<Tuple> iter) throws IOException {
        if (!iter.hasNext()) {
            return new SetStringValueData(Collections.<String>emptyList());
        }

        Set<String> result = new HashSet<>();
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
