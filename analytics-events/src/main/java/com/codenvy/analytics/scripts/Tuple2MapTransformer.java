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
import org.apache.pig.impl.util.TupleFormat;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class Tuple2MapTransformer implements TupleTransformer {

    /** {@inheritedDoc) */
    @Override
    public Map<String, Long> transform(Tuple tuple) throws IOException {
        TupleFormat.format(tuple);
        
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
}
