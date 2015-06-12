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
package com.codenvy.api.subscription.saas.server.billing;

import java.util.Objects;

/**
 * @author Sergii Leschenko
 */
public class Bonus {
    private long   id;
    private String accountId;
    private double resources;
    private long   tillDate;
    private long   fromDate;
    private String cause;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Bonus withId(long id) {
        this.id = id;
        return this;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Bonus withAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public Double getResources() {
        return resources;
    }

    public void setResources(Double resources) {
        this.resources = resources;
    }

    public Bonus withResources(Double resources) {
        this.resources = resources;
        return this;
    }

    public long getTillDate() {
        return tillDate;
    }

    public void setTillDate(long tillDate) {
        this.tillDate = tillDate;
    }

    public Bonus withTillDate(long tillDate) {
        this.tillDate = tillDate;
        return this;
    }

    public long getFromDate() {
        return fromDate;
    }

    public void setFromDate(long fromDate) {
        this.fromDate = fromDate;
    }

    public Bonus withFromDate(long fromDate) {
        this.fromDate = fromDate;
        return this;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public Bonus withCause(String cause) {
        this.cause = cause;
        return this;
    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (int)(id ^ id >>> 32);
        hash = 31 * hash + Objects.hashCode(accountId);
        hash = 31 * hash + (int)(fromDate ^ fromDate >>> 32);
        hash = 31 * hash + (int)(tillDate ^ tillDate >>> 32);
        final long resourcesBits = Double.doubleToLongBits(resources);
        hash = 31 * hash + (int)(resourcesBits ^ resourcesBits >>> 32);
        hash = 31 * hash + Objects.hashCode(cause);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Bonus)) {
            return false;
        }
        final Bonus other = (Bonus)obj;
        return id == other.id &&
               Objects.equals(accountId, other.accountId) &&
               resources == other.resources &&
               fromDate == other.fromDate &&
               tillDate == other.tillDate &&
               Objects.equals(cause, other.cause);
    }
}
