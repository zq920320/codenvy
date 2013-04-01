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

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public class BasePigTest
{
    /**
     * Relative path to temporary files.
     */
    public static final String BASE_DIR = "target";

    protected void executePigScript(ScriptType type, File log, Map<String, String> executionParams) throws IOException
    {
        executionParams.put(Constants.LOG, log.getAbsolutePath());

        ScriptExecutor scriptExecutor = new ScriptExecutor(type);
        scriptExecutor.setParams(executionParams);

        FileObject fileObject = scriptExecutor.executeAndReturnResult(BASE_DIR);
        fileObject.store();
    }

    protected FileObject executeAndReturnResult(ScriptType type, File log, Map<String, String> executionParams)
                                                                                                               throws IOException
    {
        executionParams.put(Constants.LOG, log.getAbsolutePath());

        ScriptExecutor scriptExecutor = new ScriptExecutor(type);
        scriptExecutor.setParams(executionParams);

        return scriptExecutor.executeAndReturnResult(BASE_DIR);
    }
}
