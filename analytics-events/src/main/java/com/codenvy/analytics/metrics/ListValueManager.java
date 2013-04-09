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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ListValueManager implements ValueManager {

    @Override
    public Object load(BufferedReader reader) throws IOException {
        List<String> result = new ArrayList<String>();

        String line;
        while ((line = reader.readLine()) != null) {
            result.add(line);
        }

        return Collections.unmodifiableList(result);
    }

    @Override
    public void store(BufferedWriter writer, Object value) throws IOException {
        if (value instanceof List) {
            List<String> props = (List<String>)value;

            for (String line : props) {
                writer.write(line);
                writer.newLine();
            }

            writer.flush();
        } else {
            throw new IOException("Unknown class " + value.getClass().getName() + " for storing");
        }
    }
}
