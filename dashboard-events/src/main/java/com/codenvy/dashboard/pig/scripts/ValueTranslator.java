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

import org.apache.pig.data.Tuple;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * It is used to translate result received from script execution
 * into the object more useful to operate with. Also it has 
 * responsibilities to perform read and write in the way more
 * suitable for given object.
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public interface ValueTranslator
{
   /**
    * Performs write operation. It is supposed the value already translated.
    */
   void doWrite(BufferedWriter writer, Object value) throws IOException;

   /**
    * Performs read operation. 
    * @return the translated object. 
    */
   Object doRead(BufferedReader reader) throws IOException;

   /** 
    * Object translation.
    * 
    * @param value some object from resulted {@link Tuple}
    * @return translated object
    */
   Object translate(Object value) throws IOException;
}
