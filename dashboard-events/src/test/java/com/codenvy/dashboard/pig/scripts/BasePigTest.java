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

import org.apache.pig.ExecType;
import org.apache.pig.PigServer;
import org.apache.pig.data.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public class BasePigTest
{
   public static final String BASE_DIR = "target";

   protected static final Logger LOG = LoggerFactory.getLogger(BasePigTest.class);

   private PigServer pigServer;

   @BeforeMethod
   public void setUpMethod() throws Exception
   {
      pigServer = new PigServer(ExecType.LOCAL);
   }

   @AfterMethod
   public void tearDownMethod() throws Exception
   {
      pigServer.shutdown();
   }

   /**
    * Run pig script with parameters. Returns iterator by {@link PigConstants#FINAL_RELATION}.
    */
   public Iterator<Tuple> runPigScriptAndGetResult(String script, File log, String[][] data) throws IOException
   {
      InputStream scriptContent = Thread.currentThread().getContextClassLoader().getResourceAsStream(script);
      scriptContent = replaceImportCommand(scriptContent);

      Map<String, String> params = new HashMap<String, String>(data.length);
      params.put(PigConstants.LOG_PARAM, log.getAbsolutePath());
      params.put(PigConstants.STORE_INTO_PARAM, BASE_DIR);

      for (String[] str : data)
      {
         params.put(str[0], str[1]);
      }

      pigServer.registerScript(scriptContent, params);
      return pigServer.openIterator(PigConstants.FINAL_RELATION);
   }

   /**
    * Run pig script with parameters.
    */
   public void runPigScript(String script, File log, String[][] data) throws IOException
   {
      InputStream scriptContent = Thread.currentThread().getContextClassLoader().getResourceAsStream(script);
      scriptContent = replaceImportCommand(scriptContent);

      Map<String, String> params = new HashMap<String, String>(data.length);
      params.put(PigConstants.LOG_PARAM, log.getAbsolutePath());
      params.put(PigConstants.STORE_INTO_PARAM, BASE_DIR);

      for (String[] str : data)
      {
         params.put(str[0], str[1]);
      }

      pigServer.registerScript(scriptContent, params);
   }

   /**
    * Reads a stream until its end and returns its content as a byte array. 
    */
   private byte[] getStreamContentAsBytes(InputStream is) throws IOException, IllegalArgumentException
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
   private String getStreamContentAsString(InputStream is) throws IOException
   {
      byte[] bytes = getStreamContentAsBytes(is);
      return new String(bytes, "UTF-8");
   }

   private InputStream replaceImportCommand(InputStream scriptContent) throws IOException, UnsupportedEncodingException
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource("macros.pig");
      String content = getStreamContentAsString(scriptContent);
      content = content.replace("macros.pig", url.getFile());

      return new ByteArrayInputStream(content.getBytes("UTF-8"));
   }
}
