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
package com.codenvy.dashboard.scripts;

import com.codenvy.dashboard.scripts.Constants;
import com.codenvy.dashboard.scripts.ScriptExecutor;
import com.codenvy.dashboard.scripts.ScriptType;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public class BasePigTest
{
   /**
    * Relative path to temporary files.
    */
   public static final String BASE_DIR = "target";

   protected void executePigScript(ScriptType type, File log, String[][] params) throws IOException
   {
      ScriptExecutor scriptExecutor = new ScriptExecutor(type);
      scriptExecutor.setParam(Constants.LOG, log.getAbsolutePath());
      for (String[] param : params)
      {
         scriptExecutor.setParam(param[0], param[1]);
      }

      scriptExecutor.executeAndStoreResult(BASE_DIR);
   }
}
