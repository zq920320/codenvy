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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
@SuppressWarnings("serial")
public class ImmutableProperties extends Properties
{

   /**
    * {@link ImmutableProperties} constructor.
    */
   public ImmutableProperties(Properties original)
   {
      super();

      for (java.util.Map.Entry<Object, Object> entry : original.entrySet())
      {
         super.put(entry.getKey(), entry.getValue());
      }
   }


   /**
    * {@inheritedDoc)
    */
   @Override
   public Object setProperty(String key, String val)
   {
      throw new UnsupportedOperationException("Method is not supported");
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public synchronized void load(Reader arg0) throws IOException
   {
      throw new UnsupportedOperationException("Method is not supported");
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public synchronized void load(InputStream arg0) throws IOException
   {
      throw new UnsupportedOperationException("Method is not supported");
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public synchronized void loadFromXML(InputStream arg0) throws IOException, InvalidPropertiesFormatException
   {
      throw new UnsupportedOperationException("Method is not supported");
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public synchronized void clear()
   {
      throw new UnsupportedOperationException("Method is not supported");
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public synchronized Object put(Object arg0, Object arg1)
   {
      throw new UnsupportedOperationException("Method is not supported");
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public synchronized void putAll(Map<? extends Object, ? extends Object> arg0)
   {
      throw new UnsupportedOperationException("Method is not supported");
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public synchronized Object remove(Object arg0)
   {
      throw new UnsupportedOperationException("Method is not supported");
   }
}
