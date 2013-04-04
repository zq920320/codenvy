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
package com.codenvy.dashboard.scripts;

import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Translate any {@link DataBag} into the {@link List}.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class Bag2ListTranslator implements ValueTranslator {

    /** {@inheritDoc} */
    @Override
    public void doWrite(BufferedWriter writer, Object value) throws IOException {
        if (value instanceof List) {
            List<String> list = (List<String>)value;
            for (String str : list) {
                writer.write(str);
                writer.newLine();
            }

            writer.flush();
        } else {
            throw new IOException("Unknown class " + value.getClass().getName() + " for storing");
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<String> doRead(BufferedReader reader) throws IOException {
        List<String> result = new ArrayList<String>();

        String line;
        while ((line = reader.readLine()) != null) {
            result.add(line);
        }

        return Collections.unmodifiableList(result);
    }

    /** {@inheritDoc} */
    @Override
    public List<String> translate(Object value) throws IOException {
        if (value instanceof DataBag) {
            DataBag bag = (DataBag)value;

            List<String> result = new ArrayList<String>((int)bag.size());

            Iterator<Tuple> iter = bag.iterator();
            while (iter.hasNext()) {
                Tuple tuple = iter.next();
                if (tuple.size() != 1) {
                    throw new IOException("Tuple size should be 1");
                }

                result.add(tuple.get(0).toString());
            }

            return Collections.unmodifiableList(result);
        }

        throw new IOException("Unknown class " + value.getClass().getName() + " for translation");
    }
}
