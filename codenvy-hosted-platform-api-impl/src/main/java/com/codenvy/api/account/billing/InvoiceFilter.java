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
 * Filter for invoice queries.
 *
 * @author Sergii Kabashniuk
 */
public class InvoiceFilter {
    private Long     id;
    private String   accountId;
    private String[] states;
    private Boolean  isMailNotSend;
    private Integer  maxItems;
    private Integer  skipCount;
    private Long     fromDate;
    private Long     untilDate;

    private InvoiceFilter() {
    }


    public String getAccountId() {
        return accountId;
    }


    public Long getId() {
        return id;
    }

    public String[] getStates() {
        return states;
    }

    public Boolean getIsMailNotSend() {
        return isMailNotSend;
    }

    public Integer getMaxItems() {
        return maxItems;
    }

    public Integer getSkipCount() {
        return skipCount;
    }

    public Long getFromDate() {
        return fromDate;
    }


    public Long getUntilDate() {
        return untilDate;
    }


    public static Builder builder() {
        return new InvoiceFilter.Builder();
    }

    public static class Builder {
        private InvoiceFilter instance = new InvoiceFilter();

        private Builder() {
        }

        public Builder withId(long id) {
            instance.id = id;
            return this;
        }

        public Builder withAccountId(String accountId) {
            instance.accountId = accountId;
            return this;
        }

        public Builder withPaymentStates(PaymentState... states) {
            if (states.length == 0) {
                throw new RuntimeException("Invalid value " + states);
            }
            instance.states = new String[states.length];
            for (int i = 0; i < states.length; i++) {
                instance.states[i] = states[i].getState();
            }
            return this;
        }

        public Builder withIsMailNotSend() {
            instance.isMailNotSend = true;
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

        public Builder withFromDate(long fromDate) {
            if (fromDate > 0) {
                instance.fromDate = fromDate;
            }
            return this;
        }

        public Builder withUntilDate(long untilDate) {
            if (untilDate > 0) {
                instance.untilDate = untilDate;
            }
            return this;
        }

        public InvoiceFilter build() {
            return instance;
        }
    }
}
