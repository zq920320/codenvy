/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.modules.pigexecutor.config;

import java.util.ArrayList;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class PigScriptsExecutorConfiguration {
    
    private ArrayList<ExecutionEntry> executions;
    
    /**
     * Empty constructor. 
     */
    public PigScriptsExecutorConfiguration() {
    }

    /**
     * @return the executions
     */
    public ArrayList<ExecutionEntry> getExecutions() {
        return executions;
    }

    /**
     * @param executions the executions to set
     */
    public void setExecutions(ArrayList<ExecutionEntry> executions) {
        this.executions = executions;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(500);
        sb.append("{PigScriptsExecutorConfiguration : ").append("\n");
        
        for (ExecutionEntry executionEntry : executions) {
            sb.append(executionEntry.toString()).append("\n");
        }
        
        sb.append("}");
        
        return sb.toString();
    }
}
