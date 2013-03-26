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

import java.util.Map;

/**
 * Simple POJO object containing all necessary data for execution
 * particular script.
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ScriptExecutionContext
{
   /**
    * Script execution result. 
    */
   private Object result;

   /**
    * Runtime script parameters.
    */
   private Map<String, String> params;

   /**
    * {@link ScriptType}.
    */
   private ScriptType scriptType;

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

   /**
    * Getter for {@link #scriptType}. 
    */
   public ScriptType getScriptType()
   {
      return scriptType;
   }

   /**
    * Setter for {@link #scriptType}. 
    */
   public void setScriptType(ScriptType type)
   {
      this.scriptType = type;
   }

   /**
    * Getter for {@link #params}. 
    */
   public Map<String, String> getParams()
   {
      return params;
   }

   /**
    * Setter for {@link #params}. 
    */
   public void setParams(Map<String, String> params)
   {
      this.params = params;
   }
}
