/*
 *    Copyright (C) 2013 eXo Platform SAS.
 *
 *    This is free software; you can redistribute it and/or modify it
 *    under the terms of the GNU Lesser General Public License as
 *    published by the Free Software Foundation; either version 2.1 of
 *    the License, or (at your option) any later version.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this software; if not, write to the Free
 *    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *    02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.codenvy.analytics.scripts;

import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;

import java.io.*;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Translate any {@link DataBag} into the {@link Properties}. The key is instance of {@link String}, and the value is instance of
 * {@link String}.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class Bag2PropertiesTranslator implements ValueTranslator {

    /** {@inheritedDoc) */
    @Override
    public void doWrite(BufferedWriter writer, Object value) throws IOException {
        if (value instanceof Properties) {
            Properties props = (Properties)value;
            props.store(writer, null);
        } else {
            throw new IOException("Unknown class " + value.getClass().getName() + " for storing");
        }
    }

    /** {@inheritedDoc) */
    @Override
    public Properties doRead(BufferedReader reader) throws IOException {
        Properties props = new Properties();
        props.load(reader);

        return new ImmutableProperties(props);
    }

    /** {@inheritedDoc) */
    @Override
    public Properties translate(Object value) throws IOException {
        if (value instanceof DataBag) {
            Properties props = new Properties();

            Iterator<Tuple> iter = ((DataBag)value).iterator();
            while (iter.hasNext()) {
                Tuple tuple = iter.next();

                if (tuple.size() != 2) {
                    throw new IOException("Tuple size should be 2");
                }

                props.put(tuple.get(0).toString(), tuple.get(1).toString());
            }

            return new ImmutableProperties(props);
        }

        throw new IOException("Unknown class " + value.getClass().getName() + " for translation");
    }

    /** Immutable wrapper for {@link Properties}. */
    @SuppressWarnings("serial")
    private class ImmutableProperties extends Properties {

        /** {@link ImmutableProperties} constructor. */
        public ImmutableProperties(Properties original) {
            super();

            for (java.util.Map.Entry<Object, Object> entry : original.entrySet()) {
                super.put(entry.getKey(), entry.getValue());
            }
        }

        /** {@inheritedDoc) */
        @Override
        public Object setProperty(String key, String val) {
            throw new UnsupportedOperationException("Method is not supported");
        }

        /** {@inheritedDoc) */
        @Override
        public synchronized void load(Reader arg0) throws IOException {
            throw new UnsupportedOperationException("Method is not supported");
        }

        /** {@inheritedDoc) */
        @Override
        public synchronized void load(InputStream arg0) throws IOException {
            throw new UnsupportedOperationException("Method is not supported");
        }

        /** {@inheritedDoc) */
        @Override
        public synchronized void loadFromXML(InputStream arg0) throws IOException, InvalidPropertiesFormatException {
            throw new UnsupportedOperationException("Method is not supported");
        }

        /** {@inheritedDoc) */
        @Override
        public synchronized void clear() {
            throw new UnsupportedOperationException("Method is not supported");
        }

        /** {@inheritedDoc) */
        @Override
        public synchronized Object put(Object arg0, Object arg1) {
            throw new UnsupportedOperationException("Method is not supported");
        }

        /** {@inheritedDoc) */
        @Override
        public synchronized void putAll(Map<? extends Object, ? extends Object> arg0) {
            throw new UnsupportedOperationException("Method is not supported");
        }

        /** {@inheritedDoc) */
        @Override
        public synchronized Object remove(Object arg0) {
            throw new UnsupportedOperationException("Method is not supported");
        }
    }
}
