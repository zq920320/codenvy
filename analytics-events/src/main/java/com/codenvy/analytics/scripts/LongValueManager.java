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

import org.apache.pig.data.Tuple;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class LongValueManager implements ValueManager {

    /** {@inheritedDoc} */
    @Override
    public Long valueOf(Tuple tuple) throws IOException {
        if (tuple == null) {
            return Long.valueOf(0);
        }

        return (Long)tuple.get(0);
    }

    /** {@inheritedDoc} */
    @Override
    public Long load(BufferedReader reader) throws IOException {
        return valueOf(reader.readLine());
    }


    /** {@inheritDoc} */
    @Override
    public void store(BufferedWriter writer, Object value) throws IOException {
        writer.write(value.toString());
        writer.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long valueOf(String value) throws IOException {
        return Long.valueOf(value);
    }
}
