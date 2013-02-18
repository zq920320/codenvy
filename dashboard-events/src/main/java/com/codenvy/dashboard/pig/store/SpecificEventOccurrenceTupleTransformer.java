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
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.Tuple;

/**
 * Transform the result of specific-event-occurrence.pig into the {@link DBObject}<br>  
 * 
 * Incoming tuple: (chararray: event, int: date, long: number)<br>
 * Outcoming jason: { "_id"   : long: ..., <br>  
 *                    "type"  : String: "SPECIFIC_EVENT_OCCURRENCE" ,<br>
 *                    "event" : String: ... ,<br>
 *                    "date"  : int: ... ,<br> 
 *                    "count" : long: ...}<br>
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class SpecificEventOccurrenceTupleTransformer extends AbstractTupleTransformer
{

   /**
    * SpecificEventOccurrenceTupleTransformer constructor.
    */
   SpecificEventOccurrenceTupleTransformer(ScriptType type)
   {
      super(type);
   }

   /**
    * {@inheritDoc}
    */
   public DBObject transform(Tuple tuple) throws ExecException
   {
      DBObject dbObject = new BasicDBObject(tuple.size() + 2);
      dbObject.put("_id", getId(tuple));
      dbObject.put("type", type.toString());
      dbObject.put("event", getEvent(tuple));
      dbObject.put("date", getDate(tuple));
      dbObject.put("count", getCount(tuple));

      return dbObject;
   }

   private long getCount(Tuple tuple) throws ExecException
   {
      return (Long)tuple.get(2);
   }

   private int getDate(Tuple tuple) throws ExecException
   {
      return (Integer)tuple.get(1);
   }

   private String getEvent(Tuple tuple) throws ExecException
   {
      return (String)tuple.get(0);
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   protected long getId(Tuple tuple) throws ExecException
   {
      long hash = getEvent(tuple).hashCode();
      hash = hash * 31 + getDate(tuple);

      return hash;
   }
}
