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
    long MBMSEC_TO_GBH_MULTIPLIER = TimeUnit.HOURS.toMillis(1) * 1024;

    String GBH_SUM =
            " SUM(ROUND(FAMOUNT * ( upper(FDURING * ?)-lower(FDURING * ?)-1 )/" + MBMSEC_TO_GBH_MULTIPLIER + ".0 ,6)) ";


    String TOTAL_SUM = "ROUND(SUM(ROUND(FPAID_AMOUNT,2)*FPAID_PRICE),2)";

    String INVOICES_FIELDS =
            "                   FID, " +
            "                   FTOTAL, " +
            "                   FACCOUNT_ID, " +
            "                   FCREDIT_CARD, " +
            "                   FPAYMENT_TIME, " +
            "                   FPAYMENT_STATE, " +
            "                   FMAILING_TIME, " +
            "                   FCREATED_TIME, " +
            "                   FPERIOD, " +
            "                   FCALC_ID ";

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
            "   FDURING && ?" +
            "GROUP BY " +
            " FACCOUNT_ID, " +
            " FWORKSPACE_ID ";

    String PREPAID_AMOUNT =
            //" SUM(ROUND(FAMOUNT * ( upper(FDURING * ?)-lower(FDURING * ?)-1 )/" + MBMSEC_TO_GBH_MULTIPLIER + ".0 ,
            // 6)) ";
            " P.FAMOUNT*(upper(P.FPERIOD * ?)-lower(P.FPERIOD * ?)-1)/? ";


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
            "   M.FACCOUNT_ID AS FACCOUNT_ID, " +
            "   ? AS FSERVICE_ID, " +
            "   LEAST(SUM(M.FAMOUNT), ?) AS FFREE_AMOUNT, " +
            "   LEAST(GREATEST(SUM(M.FAMOUNT) -?, 0), " + PREPAID_AMOUNT + ") AS FPREPAID_AMOUNT, " +
            "   GREATEST(SUM(M.FAMOUNT) - ? - "+PREPAID_AMOUNT+", 0) AS FPAID_AMOUNT, " +
            "   ? AS FPAID_PRICE, " +
            "   ? as FCALC_ID " +
            "FROM " +
            "  MEMORY_CHARGES AS M " +
            "  LEFT JOIN PREPAID AS P ON M.FACCOUNT_ID=P.FACCOUNT_ID " +
            "WHERE " +
            "  M.FCALC_ID = ? " +
            "  AND P.FPERIOD && ? " +
            "GROUP BY " +
            "  M.FACCOUNT_ID, "+
            "  P.FPERIOD,  "+
            "  P.FAMOUNT "

            ;


    /**
     * Generate invoices from charges.
     */
    String INVOICES_INSERT =
            "INSERT INTO " +
            "   INVOICES(" +
            "                   FTOTAL, " +
            "                   FACCOUNT_ID, " +
            "                   FPAYMENT_STATE, " +
            "                   FCREATED_TIME, " +
            "                   FPERIOD, " +
            "                   FCALC_ID " +
            "                  ) " +
            "SELECT " +
            "   " + TOTAL_SUM + " AS FTOTAL, " +
            "   FACCOUNT_ID AS FACCOUNT_ID, " +
            "  CASE " +
            "   WHEN " + TOTAL_SUM + "> 0.0 THEN '" + PaymentState.WAITING_EXECUTOR.getState() + "'" +
            "   ELSE  '" + PaymentState.NOT_REQUIRED.getState() + "'" +
            "  END as FPAYMENT_STATE, " +
            "   ? as FCREATED_TIME, " +
            "   ? as FPERIOD, " +
            "   ? as FCALC_ID " +
            "FROM " +
            "  CHARGES " +
            "WHERE " +
            "  FCALC_ID = ? " +
            "GROUP BY " +
            "  FACCOUNT_ID ";


    /**
     * Update payment status of invoices.
     */
    String INVOICES_PAYMENT_STATE_UPDATE = "UPDATE   INVOICES " +
                                           " SET FPAYMENT_STATE=? , FPAYMENT_TIME=?, FCREDIT_CARD=? " +
                                           " WHERE FID=? ";
    /**
     * Update mailing time of invoices.
     */
    String INVOICES_MAILING_TIME_UPDATE  = "UPDATE INVOICES " +
                                           " SET FMAILING_TIME=?" +
                                           " WHERE FID=? ";

    /**
     * Select charges by given account id and calculation id.
     */
    String CHARGES_SELECT        =
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
    String MEMORY_CHARGES_SELECT =
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
                           "      FDURING," +
                           "      FUSER_ID," +
                           "      FACCOUNT_ID," +
                           "      FWORKSPACE_ID, " +
                           "      FRUN_ID" +
                           "  )" +
                           "    VALUES (?, ?, ?, ?, ?, ? );";

    String METRIC_SELECT_ID = " SELECT " +
                              "      FAMOUNT," +
                              "      FDURING," +
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
                                         "   AND FDURING && ?";

    String METRIC_SELECT_ACCOUNT_GB_WS_TOTAL = "SELECT " +
                                               "  " + GBH_SUM + " AS FAMOUNT, " +
                                               "   FWORKSPACE_ID " +
                                               "FROM " +
                                               "  METRICS " +
                                               "WHERE " +
                                               "   FACCOUNT_ID=?" +
                                               "   AND FDURING && ?" +
                                               "GROUP BY FWORKSPACE_ID";


    String METRIC_UPDATE = "UPDATE  METRICS " +
                           " SET FDURING=? " +
                           " WHERE FID=? ";


    String PREPAID_INSERT = "INSERT INTO PREPAID " +
                            "  (" +
                            "      FACCOUNT_ID," +
                            "      FAMOUNT," +
                            "      FPERIOD," +
                            "      FADDED" +

                            "  )" +
                            "    VALUES (?, ?, ?, now());";


}
