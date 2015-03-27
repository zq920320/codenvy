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

import java.util.Date;

/**
 * Provider if billing dates information.
 *
 * @author Sergii Kabashniuk
 */
public interface BillingPeriod {
    /**
     * @return current billing period.
     */
    Period getCurrent();

    /**
     * Get period for given date.
     *
     * @param date
     *         given date
     * @return billing period.
     */
    Period get(Date date);

    /**
     * Get period for given date.
     *
     * @param date
     *         given date in milliseconds
     * @return billing period.
     */
    Period get(long date);

    /**
     * Get period by given id.
     *
     * @param id
     *         period id.
     * @return billing period
     */
    Period get(String id);
}
