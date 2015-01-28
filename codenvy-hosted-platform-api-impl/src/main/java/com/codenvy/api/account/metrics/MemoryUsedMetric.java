/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.api.account.metrics;

/**
 * @author Sergii Kabashniuk
 */
public class MemoryUsedMetric {

    private final Integer   amount;
    private final Long   startTime;
    private final Long   stopTime;
    private final String userId;
    private final String accountId;
    private final String workspaceId;
    private final String runId;

    public MemoryUsedMetric(Integer amount,
                            Long startTime,
                            Long stopTime,
                            String userId,
                            String accountId,
                            String workspaceId,
                            String runId) {
        this.amount = amount;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.userId = userId;
        this.accountId = accountId;
        this.workspaceId = workspaceId;
        this.runId = runId;
    }

    public Integer getAmount() {
        return amount;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getStopTime() {
        return stopTime;
    }

    public String getUserId() {
        return userId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getRunId() {
        return runId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MemoryUsedMetric that = (MemoryUsedMetric)o;

        if (accountId != null ? !accountId.equals(that.accountId) : that.accountId != null) return false;
        if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
        if (runId != null ? !runId.equals(that.runId) : that.runId != null) return false;
        if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) return false;
        if (stopTime != null ? !stopTime.equals(that.stopTime) : that.stopTime != null) return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (workspaceId != null ? !workspaceId.equals(that.workspaceId) : that.workspaceId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = amount != null ? amount.hashCode() : 0;
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (stopTime != null ? stopTime.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (accountId != null ? accountId.hashCode() : 0);
        result = 31 * result + (workspaceId != null ? workspaceId.hashCode() : 0);
        result = 31 * result + (runId != null ? runId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MemoryUsedMetric{");
        sb.append("amount=").append(amount);
        sb.append(", startTime=").append(startTime);
        sb.append(", stopTime=").append(stopTime);
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", accountId='").append(accountId).append('\'');
        sb.append(", workspaceId='").append(workspaceId).append('\'');
        sb.append(", runId='").append(runId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
