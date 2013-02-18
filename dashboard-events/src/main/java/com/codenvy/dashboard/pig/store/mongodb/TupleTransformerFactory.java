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
package com.codenvy.dashboard.pig.store.mongodb;

/**
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public class TupleTransformerFactory
{
   /**
    * Creates {@link TupleTransformer} based on defined {@link ScriptType}.
    * 
    * @throws IllegalArgumentException if script type is not supported
    */
   public static TupleTransformer createTupleTransformer(ScriptType type)
   {
      switch (type)
      {
         case SPECIFIC_EVENT_OCCURRENCE :
            return new SpecificEventOccurrenceTupleTransformer(type);

         default :
            throw new IllegalArgumentException("Script type " + type + " is not supported");
      }
   }
   
   /**
    * Enumeration of all Pig-latin script types in usage. 
    */
   public static enum ScriptType
   {
      SPECIFIC_EVENT_OCCURRENCE;
   }
}
