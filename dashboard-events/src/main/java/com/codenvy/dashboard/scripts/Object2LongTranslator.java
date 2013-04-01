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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Translate any {@link Object} into the {@link Long}.
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class Object2LongTranslator implements ValueTranslator {

    /**
     * {@inheritDoc}
     */
    @Override
    public void doWrite(BufferedWriter writer, Object value) throws IOException {
        writer.write(value.toString());
        writer.flush();
    }

    /**
     * {@inheritedDoc}
     */
    @Override
    public Long doRead(BufferedReader reader) throws IOException
    {
        return Long.valueOf(reader.readLine());
    }

    /**
     * {@inheritedDoc}
     */
    @Override
    public Long translate(Object value) throws IOException
    {
        return Long.valueOf(value.toString());
    }
}
