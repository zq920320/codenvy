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
package com.codenvy.dashboard.pig.store;

import com.codenvy.dashboard.pig.store.TupleTransformerFactory.ScriptType;
import com.mongodb.DBObject;

import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.Tuple;

/**
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public abstract class AbstractTupleTransformer implements TupleTransformer
{
   /**
    * Based script type.
    */
   protected final ScriptType type;

   /**
    * AbstractTupleTransformer constructor.
    */
   AbstractTupleTransformer(ScriptType type)
   {
      this.type = type;
   }
   
   /**
    * Returns value for "_id" field of {@link DBObject} instance.
    */
   protected abstract long getId(Tuple tuple) throws ExecException;
}
