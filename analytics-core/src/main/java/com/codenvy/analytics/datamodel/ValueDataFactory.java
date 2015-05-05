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

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ValueDataFactory {

    /** Instantiates default {@link com.codenvy.analytics.datamodel.ValueData}. */
    public static ValueData createDefaultValue(Class<? extends ValueData> clazz) throws IllegalArgumentException {
        if (clazz == LongValueData.class) {
            return LongValueData.DEFAULT;

        } else if (clazz == DoubleValueData.class) {
            return DoubleValueData.DEFAULT;

        } else if (clazz == StringValueData.class) {
            return StringValueData.DEFAULT;

        } else if (clazz == MapValueData.class) {
            return MapValueData.DEFAULT;

        } else if (clazz == ListValueData.class) {
            return ListValueData.DEFAULT;

        } else if (clazz == SetValueData.class) {
            return SetValueData.DEFAULT;
        }

        throw new IllegalArgumentException("Unknown class " + clazz.getName());
    }


    /** Creates appropriate {@link ValueData} based on given value. */
    public static ValueData createValueData(Object value) {
        Class<?> clazz = value.getClass();

        if (clazz == String.class) {
            return StringValueData.valueOf((String)value);

        } else if (clazz == Long.class || clazz == Integer.class || clazz == Byte.class) {
            return LongValueData.valueOf(((Number)value).longValue());

        } else if (clazz == Double.class) {
            return DoubleValueData.valueOf((Double)value);

        } else if (clazz == BasicDBList.class) {
            return StringValueData.valueOf(Arrays.toString(((BasicDBList)value).toArray()));
        }

        throw new IllegalArgumentException("Unknown class " + clazz.getName());
    }

    /**
     * Creates appropriate {@link com.codenvy.analytics.datamodel.ValueData}.
     */
    public static ValueData createdValueData(Iterator<DBObject> iterator,
                                             Class<? extends ValueData> clazz,
                                             String[] trackedFields) {

        if (clazz == LongValueData.class) {
            return createLongValueData(iterator, trackedFields);

        } else if (clazz == DoubleValueData.class) {
            return createDoubleValueData(iterator, trackedFields);

        } else if (clazz == SetValueData.class) {
            return createSetValueData(iterator, trackedFields);

        } else if (clazz == ListValueData.class) {
            return createListValueData(iterator, trackedFields);
        }

        throw new IllegalArgumentException("Unknown class " + clazz.getName());
    }

    private static ValueData createListValueData(Iterator<DBObject> iterator, String[] trackedFields) {
        return doCreateValueData(iterator, trackedFields, ListValueData.class, new CreateValueAction() {
            Map<String, ValueData> values = new LinkedHashMap<>();

            @Override
            public void accumulate(String key, Object value) {
                this.values.put(key, ValueDataFactory.createValueData(value));
            }

            @Override
            public ValueData pull() {
                try {
                    return new ListValueData(Arrays.asList(new ValueData[]{new MapValueData(values)}));
                } finally {
                    values.clear();
                }
            }
        });
    }

    private static ValueData createSetValueData(Iterator<DBObject> iterator, String[] trackedFields) {
        return doCreateValueData(iterator, trackedFields, SetValueData.class, new CreateValueAction() {
            Set<ValueData> values = new HashSet<>();

            @Override
            public void accumulate(String key, Object value) {
                this.values.add(ValueDataFactory.createValueData(value));
            }

            @Override
            public ValueData pull() {
                try {
                    return new SetValueData(values);
                } finally {
                    values.clear();
                }
            }
        });
    }

    private static ValueData createLongValueData(Iterator<DBObject> iterator, String[] trackedFields) {
        return doCreateValueData(iterator, trackedFields, LongValueData.class, new CreateValueAction() {
            long value = 0;

            @Override
            public void accumulate(String key, Object value) {
                this.value += ((Number)value).longValue();
            }

            @Override
            public ValueData pull() {
                try {
                    return new LongValueData(value);
                } finally {
                    value = 0;
                }
            }
        });
    }

    private static ValueData createDoubleValueData(Iterator<DBObject> iterator, String[] trackedFields) {
        return doCreateValueData(iterator, trackedFields, DoubleValueData.class, new CreateValueAction() {
            double value = 0;

            @Override
            public void accumulate(String key, Object value) {
                this.value += ((Number)value).doubleValue();
            }

            @Override
            public ValueData pull() {
                try {
                    return new DoubleValueData(value);
                } finally {
                    value = 0;
                }
            }
        });
    }

    /**
     * @param iterator
     *         the iterator over result set
     * @param trackedFields
     *         the list of trackedFields indicate which data to read from resulted items
     * @param clazz
     *         the resulted class of {@link ValueData}
     * @param action
     *         the delegated action, contains behavior how to created needed result depending on given clazz
     */
    private static ValueData doCreateValueData(Iterator<DBObject> iterator,
                                               String[] trackedFields,
                                               Class<? extends ValueData> clazz,
                                               CreateValueAction action) {

        ValueData result = ValueDataFactory.createDefaultValue(clazz);

        while (iterator.hasNext()) {
            DBObject dbObject = iterator.next();

            for (String key : trackedFields) {
                if (dbObject.containsField(key) && dbObject.get(key) != null) {
                    action.accumulate(key, dbObject.get(key));
                }
            }

            result = result.add(action.pull());
        }

        return result;
    }


    /**
     * Create value action.
     */
    private interface CreateValueAction {

        /**
         * Accumulates every key-value pair over every entry for single resulted item
         */
        void accumulate(String key, Object value);

        /**
         * Creates a {@link ValueData}.
         */
        ValueData pull();
    }
}
