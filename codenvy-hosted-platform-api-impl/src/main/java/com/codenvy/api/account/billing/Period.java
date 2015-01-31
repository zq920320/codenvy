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
 * Billing period.
 *
 * @author Sergii Kabashniuk
 */
public interface Period {

    /**
     * @return date of period start.
     */
    Date getStartDate();

    /**
     * @return date of period end.
     */
    Date getEndDate();

    /**
     * @return next billing period information.
     */
    Period getNextPeriod();

    /**
     * @return previous billing period information.
     */
    Period getPreviousPeriod();

    /**
     * @return period string representation.
     */
    String getId();
}
