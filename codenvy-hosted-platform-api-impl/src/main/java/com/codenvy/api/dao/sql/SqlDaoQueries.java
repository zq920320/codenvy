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
package com.codenvy.api.dao.sql;

import com.codenvy.api.account.billing.PaymentState;

import java.util.concurrent.TimeUnit;

/**
 * Set of SQL queries
 *
 * @author Sergii Kabashniuk
 */
public interface SqlDaoQueries {
    /**
     * Multiplier to transform GB/h to MB/msec back and forth.
     */
    double MBMSEC_TO_GBH_MULTIPLIER = TimeUnit.HOURS.toMillis(1) * 1024.0;

    String GBH_SUM =
            " SUM(ROUND(FAMOUNT * (LEAST(?, FSTOP_TIME) - GREATEST(?, FSTART_TIME))/" + MBMSEC_TO_GBH_MULTIPLIER + " ,6)) ";

    /**
     * SQL query to calculate memory charges metrics.
     * Metrics transformed from MB/msec  to  GB/h and rounded with precision 6 before aggregation.
     */
    String MEMORY_CHARGES_INSERT =
            "INSERT INTO " +
            "  MEMORY_CHARGES (" +
            "                   FAMOUNT, " +
            "                   FACCOUNT_ID, " +
            "                   FWORKSPACE_ID,  " +
            "                   FCALC_ID " +
            "                  ) " +
            "SELECT " +
            "  " + GBH_SUM + " AS FAMOUNT, " +
            "   FACCOUNT_ID, " +
            "   FWORKSPACE_ID,  " +
            "   ? AS FCALC_ID  " +
            "FROM " +
            "  METRICS " +
            "WHERE " +
            "   FSTART_TIME<?" +
            "   AND FSTOP_TIME>?" +
            "GROUP BY " +
            " FACCOUNT_ID, " +
            " FWORKSPACE_ID ";


    String CHARGES_MEMORY_INSERT =
            "INSERT INTO " +
            "   CHARGES (" +
            "                   FACCOUNT_ID, " +
            "                   FSERVICE_ID, " +
            "                   FFREE_AMOUNT, " +
            "                   FPREPAID_AMOUNT, " +
            "                   FPAID_AMOUNT, " +
            "                   FPAID_PRICE, " +
            "                   FCALC_ID " +
            "                  ) " +
            "SELECT " +
            "   FACCOUNT_ID AS FACCOUNT_ID, " +
            "   ? AS FSERVICE_ID, " +
            "   LEAST(SUM(FAMOUNT), ?) AS FFREE_AMOUNT, " +
            "   0 AS FPREPAID_AMOUNT, " +
            "   GREATEST(SUM(FAMOUNT) -?, 0) AS FPREPAID_AMOUNT, " +
            "   ? AS FPAID_PRICE, " +
            "   ? as FCALC_ID " +
            "FROM " +
            "  MEMORY_CHARGES " +
            "WHERE " +
            "  FCALC_ID = ? " +
            "GROUP BY " +
            "  FACCOUNT_ID ";


    /**
     * Generate invoices from charges.
     */
    String INVOICES_INSERT               =
            "INSERT INTO " +
            "   INVOICES(" +
            "                   FTOTAL, " +
            "                   FACCOUNT_ID, " +
            "                   FPAYMENT_STATE, " +
            "                   FCREATED_TIME, " +
            "                   FFROM_TIME, " +
            "                   FTILL_TIME, " +
            "                   FCALC_ID " +
            "                  ) " +
            "SELECT " +
            "   ROUND(SUM(ROUND(FPAID_AMOUNT,2)*FPAID_PRICE),2) AS FTOTAL, " +
            "   FACCOUNT_ID AS FACCOUNT_ID, " +
            "   ? as FPAYMENT_STATE, " +
            "   ? as FCREATED_TIME, " +
            "   ? as FFROM_TIME, " +
            "   ? as FTILL_TIME, " +
            "   ? as FCALC_ID " +
            "FROM " +
            "  CHARGES " +
            "WHERE " +
            "  FCALC_ID = ? " +
            "GROUP BY " +
            "  FACCOUNT_ID ";
    /**
     * Select invoices by given payment state.
     */
    String INVOICES_PAYMENT_STATE_SELECT =
            "SELECT " +
            "                   FID, " +
            "                   FTOTAL, " +
            "                   FACCOUNT_ID, " +
            "                   FFROM_TIME, " +
            "                   FTILL_TIME, " +
            "                   FCALC_ID " +
            "FROM " +
            "  INVOICES " +
            "WHERE " +
            " FPAYMENT_STATE = ? " +
            "ORDER BY " +
            " FACCOUNT_ID, " +
            " FCREATED_TIME DESC "+
            "LIMIT ? " +
            "OFFSET ? "
            ;


    /**
     * Select invoices which is not mailed.
     */
    String INVOICES_MAILING_STATE_SELECT =
            "SELECT " +
            "                   FID, " +
            "                   FTOTAL, " +
            "                   FACCOUNT_ID, " +
            "                   FFROM_TIME, " +
            "                   FTILL_TIME, " +
            "                   FCALC_ID " +
            "FROM " +
            "  INVOICES " +
            "WHERE " +
            " FMAILING_TIME IS NULL " +
            " AND  FPAYMENT_STATE IN ('" +
            PaymentState.PAYMENT_FAIL.getState() + "', '" +
            PaymentState.PAID_SUCCESSFULLY.getState() + "', '" +
            PaymentState.CREDIT_CARD_MISSING.getState() + "') " +

            "ORDER BY " +
            " FACCOUNT_ID, " +
            " FCREATED_TIME DESC "+
            "LIMIT ? " +
            "OFFSET ? "
            ;
    /**
     * Update mailing time of invoices.
     */
    String INVOICES_MAILING_TIME_UPDATE  = "UPDATE INVOICES " +
                                           " SET FMAILING_TIME=? " +
                                           " WHERE FID=? ";

    /**
     * Select invoices by given account.
     */
    String INVOICES_ACCOUNT_SELECT =
            "SELECT " +
            "                   FID, " +
            "                   FTOTAL, " +
            "                   FACCOUNT_ID, " +
            "                   FCREDIT_CARD, " +
            "                   FPAYMENT_TIME, " +
            "                   FPAYMENT_STATE, " +
            "                   FMAILING_TIME, " +
            "                   FCREATED_TIME, " +
            "                   FFROM_TIME, " +
            "                   FTILL_TIME, " +
            "                   FCALC_ID " +
            "FROM " +
            "  INVOICES " +
            "WHERE " +
            " FACCOUNT_ID = ? " +

            "ORDER BY FCREATED_TIME DESC "+
            "LIMIT ? " +
            "OFFSET ? "
            ;

    /**
     * Update payment status of invoices.
     */
    String INVOICES_PAYMENT_STATE_UPDATE = "UPDATE   INVOICES " +
                                           " SET FPAYMENT_STATE=? " +
                                           " WHERE FID=? ";
    /**
     * Select charges by given account id and calculation id.
     */
    String CHARGES_SELECT                =
            "SELECT " +
            "                   FSERVICE_ID, " +
            "                   FFREE_AMOUNT, " +
            "                   FPREPAID_AMOUNT, " +
            "                   FPAID_AMOUNT, " +
            "                   FPAID_PRICE " +
            "FROM " +
            "  CHARGES " +
            "WHERE " +
            " FACCOUNT_ID  = ? " +
            " AND FCALC_ID = ? ";
    /**
     * Select memory charges by given account id and calculation id.
     */
    String MEMORY_CHARGES_SELECT         =
            "SELECT " +
            "                   FAMOUNT, " +
            "                   FWORKSPACE_ID  " +
            "FROM " +
            "  MEMORY_CHARGES " +
            "WHERE " +
            " FACCOUNT_ID  = ? " +
            " AND FCALC_ID = ? ";


    String METRIC_INSERT = "INSERT INTO METRICS " +
                           "  (" +
                           "      FAMOUNT," +
                           "      FSTART_TIME," +
                           "      FSTOP_TIME," +
                           "      FUSER_ID," +
                           "      FACCOUNT_ID," +
                           "      FWORKSPACE_ID, " +
                           "      FBILLING_PERIOD," +
                           "      FRUN_ID" +
                           "  )" +
                           "    VALUES (?, ?, ?, ?, ?, ? , ?, ?);";

    String METRIC_SELECT_ID = " SELECT " +
                              "      FAMOUNT," +
                              "      FSTART_TIME," +
                              "      FSTOP_TIME," +
                              "      FUSER_ID," +
                              "      FACCOUNT_ID," +
                              "      FWORKSPACE_ID,  " +
                              "      FRUN_ID " +
                              "FROM " +
                              "  METRICS " +
                              "WHERE FID=?";

    String METRIC_SELECT_RUNID = " SELECT " +
                                 "      FAMOUNT," +
                                 "      FSTART_TIME," +
                                 "      FSTOP_TIME," +
                                 "      FUSER_ID," +
                                 "      FACCOUNT_ID," +
                                 "      FWORKSPACE_ID,  " +
                                 "      FRUN_ID " +
                                 "FROM " +
                                 "  METRICS " +
                                 "WHERE " +
                                 "  FRUN_ID=? " +
                                 "ORDER BY " +
                                 "  FSTART_TIME";


    String METRIC_SELECT_ACCOUNT_TOTAL = "SELECT " +
                                         "  " + GBH_SUM + " AS FAMOUNT " +
                                         "FROM " +
                                         "  METRICS " +
                                         "WHERE " +
                                         "   FACCOUNT_ID=?" +
                                         "   AND FSTART_TIME<?" +
                                         "   AND FSTOP_TIME>?";

    String METRIC_SELECT_ACCOUNT_GB_WS_TOTAL = "SELECT " +
                                               "  " + GBH_SUM + " AS FAMOUNT, " +
                                               "   FWORKSPACE_ID " +
                                               "FROM " +
                                               "  METRICS " +
                                               "WHERE " +
                                               "   FACCOUNT_ID=?" +
                                               "   AND FSTART_TIME<?" +
                                               "   AND FSTOP_TIME>? " +
                                               "GROUP BY FWORKSPACE_ID";


    String METRIC_UPDATE = "UPDATE  METRICS " +
                           " SET FSTOP_TIME=? " +
                           " WHERE FID=? ";

}
