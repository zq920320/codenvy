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
package com.codenvy.api.subscription.saas.server.dao.sql;

import static com.codenvy.api.metrics.server.dao.sql.MetricsSqlQueries.GBH_SUM;
import static com.codenvy.api.subscription.saas.server.billing.PaymentState.NOT_REQUIRED;
import static com.codenvy.api.subscription.saas.server.billing.PaymentState.WAITING_EXECUTOR;

/**
 * Set of SQL queries
 *
 * @author Sergii Kabashniuk
 */
public interface SqlDaoQueries {
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
            "   M.FACCOUNT_ID, " +
            "   M.FWORKSPACE_ID,  " +
            "   ? AS FCALC_ID  " +
            "FROM " +
            "  METRICS AS M " +
            "WHERE " +
            "   M.FDURING && ?" +
            "GROUP BY " +
            " M.FACCOUNT_ID, " +
            " M.FWORKSPACE_ID ";


    String PREPAID_AMOUNT  = " SUM(P.FAMOUNT*(upper(P.FPERIOD * ?)-lower(P.FPERIOD * ?))/?) ";
    String FFREE_AMOUNT    = "ROUND(" +
                             "      CAST(" +
                             "           LEAST(" + GBH_SUM + ", ? + CASE WHEN B.FAMOUNT IS NULL THEN 0.0 ELSE B.FAMOUNT END) " +
                             "           as numeric), " +
                             "      6) ";
    String FPREPAID_AMOUNT = "ROUND( " +
                             "      CAST( " +
                             "           LEAST( " +
                             "                 GREATEST(" + GBH_SUM + " - ?, 0), " +
                             "                 CASE WHEN P.FAMOUNT IS NULL THEN 0.0 ELSE P.FAMOUNT END) " +
                             "           as numeric), " +
                             "      6) ";
    String FPAID_AMOUNT    = "CASE WHEN P.FAMOUNT IS NULL " +
                             "  THEN 0.0 " +
                             "ELSE ROUND( " +
                             "          CAST( " +
                             "               GREATEST(" +
                             "                    " + GBH_SUM + " - ? -  P.FAMOUNT, " +
                             "                        0) " +
                             "               as numeric), " +
                             "          6)" +
                             "END";

    String BONUSES_SELECT = "SELECT " +
                            "  SUM(FAMOUNT) as FAMOUNT, " +
                            "  FACCOUNT_ID " +
                            "FROM BONUSES " +
                            "WHERE FPERIOD && ? " +
                            "GROUP BY FACCOUNT_ID";

    String ACCOUNT_BONUSES_SELECT = "SELECT " +
                                    "  SUM(FAMOUNT) as FAMOUNT, " +
                                    "  FACCOUNT_ID " +
                                    "FROM BONUSES " +
                                    "WHERE FACCOUNT_ID = ? " +
                                    "  AND FPERIOD && ? " +
                                    "GROUP BY FACCOUNT_ID";

    String EXCESSIVE_ACCOUNT_USAGE_SELECT = "SELECT " +
                                            "  CASE WHEN A.FPERIOD IS NULL " +
                                            "    THEN ROUND( " +
                                            "               CAST( " +
                                            "                   GREATEST(" +
                                            "                       " + GBH_SUM + " - ? " +
                                            "                             - CASE WHEN B.FAMOUNT IS NULL THEN 0.0 ELSE B.FAMOUNT END " +
                                            "                             - CASE WHEN P.FAMOUNT IS NULL THEN 0.0 ELSE P.FAMOUNT END, " +
                                            "                           0) " +
                                            "                   as numeric), " +
                                            "               6) " +
                                            "    ELSE 0.0" +
                                            "  END " +
                                            "  AS FPAID_AMOUNT " +
                                            "FROM " +
                                            "    METRICS AS M " +
                                            "LEFT JOIN ( " +
                                            "        SELECT " +
                                            "        " + PREPAID_AMOUNT + " AS FAMOUNT, " +
                                            "            FACCOUNT_ID " +
                                            "        FROM  " +
                                            "          PREPAID AS P " +
                                            "        WHERE " +
                                            "          P.FPERIOD && ? " +
                                            "        GROUP BY P.FACCOUNT_ID " +
                                            "      ) AS P  " +
                                            "      ON M.FACCOUNT_ID = P.FACCOUNT_ID " +
                                            "LEFT JOIN (" +
                                            "            SELECT FPERIOD, FACCOUNT_ID" +
                                            "            FROM PREPAID " +
                                            "            WHERE FPERIOD @> ? AND FACCOUNT_ID=? " +
                                            "            GROUP BY FACCOUNT_ID, " +
                                            "                     FPERIOD " +
                                            "            LIMIT 1) AS A " +
                                            "      ON A.FACCOUNT_ID = M.FACCOUNT_ID " +
                                            "LEFT JOIN  ( " + ACCOUNT_BONUSES_SELECT + " ) " +
                                            "      AS B ON M.FACCOUNT_ID = B.FACCOUNT_ID " +
                                            "WHERE M.FDURING && ? " +
                                            "  AND M.FACCOUNT_ID = ? " +
                                            "GROUP BY M.FACCOUNT_ID, " +
                                            "         P.FAMOUNT," +
                                            "         A.FPERIOD," +
                                            "         B.FAMOUNT";

    String ACCOUNTS_USAGE_SELECT = "SELECT " +
                                   "   M.FACCOUNT_ID           AS FACCOUNT_ID, " +
                                   "   " + FFREE_AMOUNT + "    AS FFREE_AMOUNT, " +
                                   "   " + FPREPAID_AMOUNT + " AS FPREPAID_AMOUNT, " +
                                   "   " + FPAID_AMOUNT + "    AS FPAID_AMOUNT " +
                                   "FROM " +
                                   "  METRICS  AS M " +
                                   "LEFT JOIN ( " +
                                   "            SELECT " +
                                   "           " + PREPAID_AMOUNT + " AS FAMOUNT, " +
                                   "               FACCOUNT_ID " +
                                   "            FROM  " +
                                   "               PREPAID AS P" +
                                   "            WHERE  " +
                                   "               P.FPERIOD && ? " +
                                   "            GROUP BY P.FACCOUNT_ID " +
                                   "           ) " +
                                   "    AS P ON M.FACCOUNT_ID = P.FACCOUNT_ID " +
                                   "LEFT JOIN ( " + BONUSES_SELECT + " )" +
                                   "    AS B ON M.FACCOUNT_ID = B.FACCOUNT_ID ";

    String CHARGES_MEMORY_INSERT =
            "INSERT INTO " +
            "   CHARGES (" +
            "                   FACCOUNT_ID, " +
            "                   FSERVICE_ID, " +
            "                   FPROVIDED_FREE_AMOUNT, " +
            "                   FPROVIDED_PREPAID_AMOUNT, " +
            "                   FFREE_AMOUNT, " +
            "                   FPREPAID_AMOUNT, " +
            "                   FPAID_AMOUNT, " +
            "                   FPAID_PRICE, " +
            "                   FCALC_ID " +
            "                  ) " +
            "SELECT " +
            "   M.FACCOUNT_ID AS FACCOUNT_ID, " +
            "   ? AS FSERVICE_ID, " +
            "   ? + CASE WHEN B.FAMOUNT IS NULL THEN 0.0 ELSE B.FAMOUNT END AS FPROVIDED_FREE_AMOUNT, " +
            "   CASE WHEN P.FAMOUNT IS NULL THEN 0.0 ELSE P.FAMOUNT END AS FPROVIDED_PREPAID_AMOUNT, " +
            "   LEAST(SUM(M.FAMOUNT), ? + CASE WHEN B.FAMOUNT IS NULL THEN 0.0 ELSE B.FAMOUNT END) AS FFREE_AMOUNT, " +
            "   LEAST(GREATEST(SUM(M.FAMOUNT) - ? - CASE WHEN B.FAMOUNT IS NULL THEN 0.0 ELSE B.FAMOUNT END, 0), CASE WHEN P.FAMOUNT IS NULL THEN 0.0 ELSE P.FAMOUNT END) AS FPREPAID_AMOUNT, " +
            "   GREATEST(SUM(M.FAMOUNT) - ? - CASE WHEN B.FAMOUNT IS NULL THEN 0.0 ELSE B.FAMOUNT END - CASE WHEN P.FAMOUNT IS NULL THEN 0.0 ELSE P.FAMOUNT END , 0) AS FPAID_AMOUNT, " +
            "   ? AS FPAID_PRICE, " +
            "   ? as FCALC_ID " +
            "FROM " +
            "  MEMORY_CHARGES AS M " +
            "  INNER JOIN ( " +
            "      SELECT " +
            "        " + PREPAID_AMOUNT + " AS FAMOUNT, " +
            "        FACCOUNT_ID " +
            "      FROM  " +
            "        PREPAID AS P" +
            "      WHERE  " +
            "        P.FPERIOD && ? " +
            "      GROUP BY P.FACCOUNT_ID " +
            "             ) " +
            "       AS P  " +
            "       ON M.FACCOUNT_ID = P.FACCOUNT_ID " +
            "  LEFT JOIN ( " + BONUSES_SELECT + " ) " +
            "      AS B ON M.FACCOUNT_ID = B.FACCOUNT_ID " +
            "WHERE " +
            "  M.FCALC_ID = ? " +
            "GROUP BY " +
            "  M.FACCOUNT_ID, " +
            "  P.FAMOUNT, " +
            "  B.FAMOUNT ";

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
            "   WHEN " + TOTAL_SUM + "> 0.0 THEN '" + WAITING_EXECUTOR.getState() + "'" +
            "   ELSE  '" + NOT_REQUIRED.getState() + "'" +
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
     * Update payment status and credit card of invoices.
     */
    String INVOICES_PAYMENT_STATE_AND_CC_UPDATE = "UPDATE   INVOICES " +
                                                  " SET FPAYMENT_STATE=? , FPAYMENT_TIME=NOW(), FCREDIT_CARD=? " +
                                                  " WHERE FID=? ";
    /**
     * Update payment status of invoices.
     */
    String INVOICES_PAYMENT_STATE_UPDATE        = "UPDATE   INVOICES " +
                                                  " SET FPAYMENT_STATE=? , FPAYMENT_TIME=NOW() " +
                                                  " WHERE FID=? ";
    /**
     * Update mailing time of invoices.
     */
    String INVOICES_MAILING_TIME_UPDATE         = "UPDATE INVOICES " +
                                                  " SET FMAILING_TIME=NOW()" +
                                                  " WHERE FID=? ";

    /**
     * Select charges by given account id and calculation id.
     */
    String CHARGES_SELECT        =
            "SELECT " +
            "                   FSERVICE_ID, " +
            "                   FPROVIDED_FREE_AMOUNT, " +
            "                   FPROVIDED_PREPAID_AMOUNT, " +
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


    String PREPAID_INSERT = "INSERT INTO PREPAID " +
                            "  (" +
                            "      FACCOUNT_ID," +
                            "      FAMOUNT," +
                            "      FPERIOD," +
                            "      FADDED" +

                            "  )" +
                            "    VALUES (?, ?, ?, now());";

    String PREPAID_CLOSE_PERIOD = "UPDATE prepaid " +
                                  "SET fperiod=int8range(selected.period, ?) " +
                                  "FROM " +
                                  "  (SELECT prepaid.fid AS id, " +
                                  "                         lower(prepaid.fperiod) AS period " +
                                  "   FROM prepaid " +
                                  "   WHERE prepaid.faccount_id=? " +
                                  "   ORDER BY upper(prepaid.fperiod) DESC LIMIT 1) selected " +
                                  "WHERE prepaid.fid = selected.id ";
}
