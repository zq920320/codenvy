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
package com.codenvy.analytics.metrics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class MapValueManager implements ValueManager {

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
}
