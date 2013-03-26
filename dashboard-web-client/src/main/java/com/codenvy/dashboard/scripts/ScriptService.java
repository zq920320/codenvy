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

import org.apache.pig.data.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
@Path("script-service")
public class ScriptService
{
   /**
    * Logger.
    */
   private static final Logger LOG = LoggerFactory.getLogger(ScriptService.class);

   /**
    * Runtime parameter name. Contains the directory where logs are located.
    */
   public static final String DASHBOARD_LOGS_DIRECTORY_PROPERTY = "dashboard.logs.directory";

   /**
    * The value of {@value #DASHBOARD_LOGS_DIRECTORY_PROPERTY} runtime parameter.
    */
   public static final String LOGS_DIRECTORY = System.getProperty(DASHBOARD_LOGS_DIRECTORY_PROPERTY);

   /**
    * Executes script and returns result in JSON.
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("execute")
   public Response execute(ScriptExecutionContext context) throws IOException
   {
      LOG.info("Execution " + context.getScriptType().getScriptFileName() + " is started");
      
      ScriptExecutor executor = new ScriptExecutor(context.getScriptType());
      executor.setParams(context.getParams());
      executor.setParam(Constants.LOG, LOGS_DIRECTORY);

      context.setResult(doExecuteScript(context, executor));

      return Response.status(Response.Status.OK).entity(context).build();
   }

   private Object doExecuteScript(ScriptExecutionContext context, ScriptExecutor executor) throws IOException
   {
      Tuple tuple = executor.executeAndReturnResult();
      FileObject fileObject = context.getScriptType().createFileObject(".", tuple);

      return fileObject.getValue();
   }
}
