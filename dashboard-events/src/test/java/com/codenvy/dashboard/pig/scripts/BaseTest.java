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
package com.codenvy.dashboard.pig.scripts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public class BaseTest
{
   protected static final Logger LOG = LoggerFactory.getLogger(BaseTest.class);

   /**
    * Reads a stream until its end and returns its content as a byte array. 
    */
   public byte[] getStreamContentAsBytes(InputStream is) throws IOException, IllegalArgumentException
   {
      try
      {
         ByteArrayOutputStream output = new ByteArrayOutputStream();
         byte[] data = new byte[8192];

         int read;
         while ((read = is.read(data)) > -1)
         {
            output.write(data, 0, read);
         }
         return output.toByteArray();
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (IOException ignore)
            {
               if (LOG.isTraceEnabled())
               {
                  LOG.trace("An exception occurred: " + ignore.getMessage());
               }
            }
            catch (RuntimeException ignore)
            {
               if (LOG.isTraceEnabled())
               {
                  LOG.trace("An exception occurred: " + ignore.getMessage());
               }
            }
         }
      }
   }

   /**
    * Returns the content of the specified stream as a string using the <code>UTF-8</code> charset.
    */
   public String getStreamContentAsString(InputStream is) throws IOException
   {
      byte[] bytes = getStreamContentAsBytes(is);
      return new String(bytes, "UTF-8");
   }
}
