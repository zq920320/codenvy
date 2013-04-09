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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class Tuple2ListTransformer implements TupleTransformer {

    /** {@inheritDoc} */
    @Override
    public List<String> transform(Tuple tuple) throws IOException {
        if (tuple == null) {
            return Collections.emptyList();
        }

        if (tuple.get(0) instanceof DataBag) {
            DataBag bag = (DataBag)tuple.get(0);

            List<String> result = new ArrayList<String>((int)bag.size());

            Iterator<Tuple> iter = bag.iterator();
            while (iter.hasNext()) {
                Tuple innterTuple = iter.next();
                if (innterTuple.size() != 1) {
                    throw new IOException("Tuple size should be 1");
                }

                result.add(innterTuple.get(0).toString());
            }

            return Collections.unmodifiableList(result);
        }

        throw new IOException("Unknown class " + tuple.getClass().getName() + " for transformation");
    }
}
