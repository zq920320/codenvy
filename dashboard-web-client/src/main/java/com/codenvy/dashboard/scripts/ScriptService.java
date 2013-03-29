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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
@Path("script-service")
@RolesAllowed({"admins", "cloud/admin", "cloud/manager"})
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
    * Runtime parameter name. Contains the directory where results are stored.
    */
   public static final String DASHBOARD_RESULT_DIRECTORY_PROPERTY = "dashboard.result.directory";

   /**
    * The value of {@value #DASHBOARD_LOGS_DIRECTORY_PROPERTY} runtime parameter.
    */
   public static final String LOGS_DIRECTORY = System.getProperty(DASHBOARD_LOGS_DIRECTORY_PROPERTY);

   /**
    * The value of {@value #DASHBOARD_RESULT_DIRECTORY_PROPERTY} runtime parameter.
    */
   public static final String RESULT_DIRECTORY = System.getProperty(DASHBOARD_RESULT_DIRECTORY_PROPERTY);

   /**
    * Executes script and returns result in JSON.
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{script}")
   public Response execute(@PathParam("script") String script, @Context UriInfo info) throws IOException
   {
      Map<String, String> params = new HashMap<String, String>();

      MultivaluedMap<String, String> queryParameters = info.getQueryParameters();
      for (String key : queryParameters.keySet())
      {
         params.put(key, queryParameters.getFirst(key));
      }

      return doExecute(script, params);
   }

   private Response doExecute(String script, Map<String, String> params) throws IOException
   {
      params.put(Constants.LOG, LOGS_DIRECTORY);

      ScriptExecutionResult executionResult = new ScriptExecutionResult();
      executionResult.setResult(getResult(script, params));

      return Response.status(Response.Status.OK).entity(executionResult).build();
   }

   private Object getResult(String script, Map<String, String> params) throws IOException
   {
      ScriptType scriptType = ScriptType.valueOf(script.toUpperCase());

      try
      {
         return getExistedResult(scriptType, params);
      }
      catch (IOException e)
      {
         return getResultFromQuery(scriptType, params);
      }
   }

   private Object getExistedResult(ScriptType scriptType, Map<String, String> params) throws IOException
   {
      FileObject fileObject = scriptType.createFileObject(RESULT_DIRECTORY, params);

      LOG.info("Result for " + scriptType.getScriptFileName() + " is returned from storage");
      return fileObject.getValue();
   }

   private Object getResultFromQuery(ScriptType scriptType, Map<String, String> params) throws IOException
   {
      ScriptExecutor executor = new ScriptExecutor(scriptType);
      executor.setParams(params);

      LOG.info("Execution " + scriptType.getScriptFileName() + " is started");

      FileObject fileObject = executor.executeAndReturnResult(RESULT_DIRECTORY);
      fileObject.store();

      LOG.info("Execution " + scriptType.getScriptFileName() + " is finished");

      return fileObject.getValue();
   }

   /**
    * Wraps result in POJO.
    */
   public class ScriptExecutionResult
   {
      /**
       * Script execution result. 
       */
      private Object result;

      /**
       * Getter for {@link #result}. 
       */
      public Object getResult()
      {
         return result;
      }

      /**
       * Setter for {@link #result}. 
       */
      public void setResult(Object result)
      {
         this.result = result;
      }
   }
}
