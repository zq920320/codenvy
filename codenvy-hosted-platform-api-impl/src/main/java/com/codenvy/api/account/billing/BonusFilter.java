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
package com.codenvy.api.account.billing;

/**
 * Filter for bonuses queries.
 *
 * @author Sergii Leschenko
 */
public class BonusFilter {
    private String  accountId;
    private Long    fromDate;
    private Long    tillDate;
    private Integer maxItems;
    private Integer skipCount;
    private String  cause;

    private BonusFilter() {
    }

    public Integer getMaxItems() {
        return maxItems;
    }

    public Integer getSkipCount() {
        return skipCount;
    }

    public String getAccountId() {
        return accountId;
    }

    public Long getFromDate() {
        return fromDate;
    }

    public Long getTillDate() {
        return tillDate;
    }

    public String getCause() {
        return cause;
    }

    public static Builder builder() {
        return new BonusFilter.Builder();
    }

    public static class Builder {
        private BonusFilter instance = new BonusFilter();

        private Builder() {
        }

        public Builder withAccountId(String accountId) {
            instance.accountId = accountId;
            return this;
        }

        public Builder withFromDate(long fromDate) {
            if (fromDate > 0) {
                instance.fromDate = fromDate;
            }
            return this;
        }

        public Builder withTillDate(long tillDate) {
            if (tillDate > 0) {
                instance.tillDate = tillDate;
            }
            return this;
        }

        public Builder withMaxItems(int maxItems) {
            if (maxItems > 0) {
                instance.maxItems = maxItems;
            }
            return this;
        }

        public Builder withSkipCount(int skipCount) {
            if (skipCount > 0) {
                instance.skipCount = skipCount;
            }
            return this;
        }

        public Builder withCause(String cause) {
            instance.cause = cause;
            return this;
        }

        public BonusFilter build() {
            return instance;
        }
    }
}
