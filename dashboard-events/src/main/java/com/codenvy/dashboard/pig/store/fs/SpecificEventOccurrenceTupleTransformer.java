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
package com.codenvy.dashboard.pig.store.fs;

import com.codenvy.dashboard.pig.store.fs.TupleTransformerFactory.ScriptType;

import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.Tuple;

import java.io.File;
import java.util.Properties;

/**
 * Transform the result of specific-event-occurrence.pig into the {@link Properties}<br>  
 * 
 * Incoming tuple: (int: date, chararray: event, long: number)<br>
 * Outcoming properties: { "type"  : String: "SPECIFIC_EVENT_OCCURRENCE" ,<br>
 *                         "event" : String: ... ,<br>
 *                         "date"  : String: ... ,<br> 
 *                         "count" : String: ...}<br>
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
   public FileObject transform(Tuple tuple) throws ExecException
   {
      String id = getId(getEvent(tuple), getDate(tuple));

      FileObject props = new FileObject(id);
      props.put("type", type.toString());
      props.put("event", getEvent(tuple));
      props.put("date", getDate(tuple).toString());
      props.put("count", getCount(tuple).toString());

      return props;
   }
   
   /**
    * Returns the unique identifier of the calculated data.
    */
   public String getId(String event, int date)
   {
      StringBuilder id = new StringBuilder();
      id.append(type.toString().toLowerCase()).append(File.separatorChar);
      id.append(event.replace('-', File.separatorChar)).append(File.separatorChar);
      id.append(parseDate(date));

      return id.toString();
   }

   private Long getCount(Tuple tuple) throws ExecException
   {
      return (Long)tuple.get(2);
   }

   private Integer getDate(Tuple tuple) throws ExecException
   {
      return (Integer)tuple.get(0);
   }

   private String getEvent(Tuple tuple) throws ExecException
   {
      return (String)tuple.get(1);
   }
}
