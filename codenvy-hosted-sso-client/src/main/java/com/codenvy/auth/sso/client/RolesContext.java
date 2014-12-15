/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.auth.sso.client;

/**
 * Holder of request context.
 * Used to store information to which workspace and account this request is belong to.
 *
 * @author Sergii Kabashniuk
 */
public class RolesContext {
    private String accountId;
    private String workspaceId;

    public RolesContext(String workspaceId, String accountId) {
        this.workspaceId = workspaceId;
        this.accountId = accountId;
    }

    public RolesContext() {
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RolesContext that = (RolesContext)o;

        if (accountId != null ? !accountId.equals(that.accountId) : that.accountId != null) return false;
        if (workspaceId != null ? !workspaceId.equals(that.workspaceId) : that.workspaceId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = accountId != null ? accountId.hashCode() : 0;
        result = 31 * result + (workspaceId != null ? workspaceId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RolesContext{" +
               "accountId='" + accountId + '\'' +
               ", workspaceId='" + workspaceId + '\'' +
               '}';
    }
}
