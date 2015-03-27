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
 * Filter for resources queries.
 *
 * @author Sergii Leschenko
 */
public class ResourcesFilter {
    private String  accountId;
    private Long    fromDate;
    private Long    tillDate;
    private Integer maxItems;
    private Integer skipCount;
    private Double  freeGbHMoreThan;
    private Double  paidGbHMoreThan;
    private Double  prePaidGbHMoreThan;

    private ResourcesFilter() {
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

    public Double getFreeGbHMoreThan() {
        return freeGbHMoreThan;
    }

    public Double getPaidGbHMoreThan() {
        return paidGbHMoreThan;
    }

    public Double getPrePaidGbHMoreThan() {
        return prePaidGbHMoreThan;
    }

    public static Builder builder() {
        return new ResourcesFilter.Builder();
    }

    public static class Builder {
        private ResourcesFilter instance = new ResourcesFilter();

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


        public Builder withFreeGbHMoreThan(double freeGbH) {
            if (freeGbH >= 0) {
                instance.freeGbHMoreThan = freeGbH;
            }
            return this;
        }

        public Builder withPaidGbHMoreThan(double paidGbH) {
            if (paidGbH >= 0) {
                instance.paidGbHMoreThan = paidGbH;
            }
            return this;
        }

        public Builder withPrePaidGbHMoreThan(double prePaidGbH) {
            if (prePaidGbH >= 0) {
                instance.prePaidGbHMoreThan = prePaidGbH;
            }
            return this;
        }

        public ResourcesFilter build() {
            return instance;
        }
    }
}
