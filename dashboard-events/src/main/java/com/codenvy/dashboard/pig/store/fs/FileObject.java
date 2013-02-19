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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
@SuppressWarnings("serial")
public class FileObject extends Properties
{

   /**
    * The file name where properties will be stored.
    */
   public static final String FILE_NAME = "value";
   
   /**
    * Contains the unique identifier of the calculated data. 
    * It will be treated as relative path to file to store properties in there.
    */
   private final String id;

   /**
    * {@link FileObject} constructor. 
    */
   public FileObject(String id)
   {
      this.id = id;
   }

   /**
    * @return {@link #id}.
    */
   public String getId()
   {
      return id;
   }
   
   /**
    * Actually stores properties into the file.
    */
   public void store(String baseDir) throws IOException
   {
      File dir = getDir(baseDir);
      if (!dir.exists())
      {
         if (!dir.mkdirs())
         {
            throw new IOException("Can not create directory tree " + dir.getAbsolutePath());
         }
      }

      File file = new File(dir, FILE_NAME);
      if (file.exists())
      {
         if (!file.delete())
         {
            throw new IOException("File " + file.getAbsolutePath() + " already exists and can not be removed");
         }
      }
      
      doStore(file);
   }

   private File getDir(String baseDir) throws IOException
   {
      if (baseDir.startsWith("file://"))
      {
         URI uri;
         try
         {
            uri = new URI(baseDir);
         }
         catch (URISyntaxException e)
         {
            throw new IOException(e);
         }

         return new File(new File(uri), id);
      }
      else
      {
         return new File(baseDir, id);
      }
   }

   private synchronized void doStore(File file) throws IOException
   {
      Writer writer = new BufferedWriter(new FileWriter(file));
      try
      {
         super.store(writer, null);
      }
      finally
      {
         writer.close();
      }
   }

   /**
    * Loads data from file.
    */
   public void load(String baseDir) throws IOException
   {
      File dir = getDir(baseDir);

      File file = new File(dir, FILE_NAME);
      if (!file.exists())
      {
         throw new IOException("File does not exist " + file.getAbsolutePath());
      }

      doLoad(file);
   }

   private synchronized void doLoad(File file) throws IOException
   {
      Reader reader = new BufferedReader(new FileReader(file));
      try
      {
         super.load(reader);
      }
      finally
      {
         reader.close();
      }
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public synchronized void load(Reader reader) throws IOException
   {
      throw new UnsupportedOperationException("Method is not supported");
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public synchronized void load(InputStream inStream) throws IOException
   {
      throw new UnsupportedOperationException("Method is not supported");
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public void store(Writer writer, String comments) throws IOException
   {
      throw new UnsupportedOperationException("Method is not supported");
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public void store(OutputStream out, String comments) throws IOException
   {
      throw new UnsupportedOperationException("Method is not supported");
   }
}
