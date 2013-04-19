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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class MapValueManager implements ValueManager {

    /** {@inheritedDoc) */
    @Override
    public Map<String, Long> valueOf(Tuple tuple) throws IOException {
        if (tuple == null) {
            return Collections.emptyMap();
        }

        if (tuple.get(0) instanceof DataBag) {
            Map<String, Long> result = new HashMap<String, Long>();

            Iterator<Tuple> iter = ((DataBag)tuple.get(0)).iterator();
            while (iter.hasNext()) {
                Tuple innterTuple = iter.next();

                if (innterTuple.size() != 2) {
                    throw new IOException("Tuple size should be 2");
                }

                result.put(innterTuple.get(0).toString(), (Long)innterTuple.get(1));
            }

            return Collections.unmodifiableMap(result);
        }

        throw new IOException("Unknown class " + tuple.getClass().getName() + " for transformation");
    }


    /**
     * {@inheritDoc}
     */
    public void store(BufferedWriter writer, Object value) throws IOException {
        if (value instanceof Map) {
            Map<String, Long> props = (Map<String, Long>)value;

            for (Entry<String, Long> entry : props.entrySet()) {
                writer.write(entry.getKey());
                writer.write("=");
                writer.write(entry.getValue().toString());
                writer.newLine();
            }

            writer.flush();
        } else {
            throw new IOException("Unknown class " + value.getClass().getName() + " for storing");
        }
    }

    /** {@inheritedDoc)  */
    @Override
    public Map<String, Long> load(BufferedReader reader) throws IOException {
        Map<String, Long> result = new HashMap<String, Long>();

        String line;
        while ((line = reader.readLine()) != null) {
            String[] entry = line.split("=");
            result.put(entry[0], Long.valueOf(entry[1]));
        }

        return Collections.unmodifiableMap(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Long> valueOf(String value) throws IOException {
        int beginIndex = value.startsWith("[") ? 1 : 0;
        int endIndex = value.endsWith("]") ? value.length() - 1 : value.length();
        value = value.substring(beginIndex, endIndex);

        String[] splittedLine = value.split(",");

        Map<String, Long> result = new LinkedHashMap<String, Long>(splittedLine.length);
        for (String str : splittedLine) {
            String[] entry = str.split("=");
            result.put(entry[0].trim(), Long.valueOf(entry[1].trim()));
        }

        return Collections.unmodifiableMap(result);
    }
}
