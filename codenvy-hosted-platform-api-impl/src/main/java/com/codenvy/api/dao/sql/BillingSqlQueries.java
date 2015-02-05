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

import java.util.concurrent.TimeUnit;

/**
 * Set of SQL queries
 *
 * @author Sergii Kabashniuk
 */
public interface BillingSqlQueries {

    long MBMSEC_TO_GBH_MULTIPLIER = 1024 * TimeUnit.HOURS.toMillis(1);

    String MEMORY_CHARGES_INSERT =
            "INSERT INTO " +
            "  MEMORY_CHARGES (" +
            "                   AMOUNT, " +
            "                   ACCOUNT_ID, " +
            "                   WORKSPACE_ID,  " +
            "                   CALC_ID " +
            "                  ) " +
            "SELECT " +
            "   SUM(ROUND(AMOUNT * (LEAST(?, STOP_TIME) - GREATEST(?, START_TIME))/" + MBMSEC_TO_GBH_MULTIPLIER + ".0 ,6)) AS AMOUNT, " +
            "   ACCOUNT_ID, " +
            "   WORKSPACE_ID,  " +
            "   ? AS CALC_ID  " +
            "FROM " +
            "  METRICS " +
            "WHERE " +
            "   START_TIME<?" +
            "   AND STOP_TIME>?" +
            "GROUP BY " +
            " ACCOUNT_ID, " +
            " WORKSPACE_ID ";


    String CHARGES_FREE_INSERT =
            "INSERT INTO " +
            "   CHARGES (" +
            "                   AMOUNT, " +
            "                   ACCOUNT_ID, " +
            "                   SERVICE_ID, " +
            "                   TYPE, " +
            "                   PRICE, " +
            "                   CALC_ID " +
            "                  ) " +
            "SELECT " +
            "   LEAST(SUM(AMOUNT), ?) AS AMOUNT, " +
            "   ACCOUNT_ID AS ACCOUNT_ID, " +
            "   ? AS SERVICE_ID, " +
            "   ? AS TYPE, " +
            "   0 AS PRICE, " +
            "   ? as CALC_ID " +
            "FROM " +
            "  MEMORY_CHARGES " +
            "WHERE " +
            "  CALC_ID = ? " +
            "GROUP BY " +
            "  ACCOUNT_ID ";


    String CHARGES_PAID_INSERT =
            "INSERT INTO " +
            "   CHARGES (" +
            "                   AMOUNT, " +
            "                   ACCOUNT_ID, " +
            "                   SERVICE_ID, " +
            "                   TYPE, " +
            "                   PRICE, " +
            "                   CALC_ID " +
            "                  ) " +
            "SELECT " +
            "   SUM(AMOUNT)-? AS AMOUNT, " +
            "   ACCOUNT_ID AS ACCOUNT_ID, " +
            "   ? AS SERVICE_ID, " +
            "   ? AS TYPE, " +
            "   ? AS PRICE, " +
            "   ? as CALC_ID " +
            "FROM " +
            "  MEMORY_CHARGES " +
            "WHERE " +
            "  CALC_ID = ? " +
            "GROUP BY " +
            "  ACCOUNT_ID " +
            "HAVING  " +
            " SUM(AMOUNT) >= ? ";


    String RECEIPTS_INSERT =
            "INSERT INTO " +
            "   RECEIPTS (" +
            "                   TOTAL, " +
            "                   ACCOUNT_ID, " +
            "                   PAYMENT_STATUS, " +
            "                   FROM_TIME, " +
            "                   TILL_TIME, " +
            "                   CALC_ID " +
            "                  ) " +
            "SELECT " +
            "   ROUND(SUM(ROUND(AMOUNT,2)*PRICE),2) AS TOTAL, " +
            "   ACCOUNT_ID AS ACCOUNT_ID, " +
            "   ? as PAYMENT_STATUS, " +
            "   ? as FROM_TIME, " +
            "   ? as TILL_TIME, " +
            "   ? as CALC_ID " +
            "FROM " +
            "  CHARGES " +
            "WHERE " +
            "  CALC_ID = ? " +
            "GROUP BY " +
            "  ACCOUNT_ID ";

    String RECEIPTS_PAYMENT_STATE_SELECT =
            "SELECT " +
            "                   ID, " +
            "                   TOTAL, " +
            "                   ACCOUNT_ID, " +
            "                   FROM_TIME, " +
            "                   TILL_TIME, " +
            "                   CALC_ID " +
            "FROM " +
            "  RECEIPTS " +
            "WHERE " +
            " PAYMENT_STATUS = ? " +
            " LIMIT ?";

    String RECEIPTS_ACCOUNT_SELECT =
            "SELECT " +
            "                   ID, " +
            "                   TOTAL, " +
            "                   PAYMENT_STATUS, " +
            "                   FROM_TIME, " +
            "                   TILL_TIME, " +
            "                   CALC_ID " +
            "FROM " +
            "  RECEIPTS " +
            "WHERE " +
            " ACCOUNT_ID = ? ";

    String CHARGES_SELECT =
            "SELECT " +
            "                   AMOUNT, " +
            "                   SERVICE_ID, " +
            "                   TYPE, " +
            "                   PRICE " +
            "FROM " +
            "  CHARGES " +
            "WHERE " +
            " ACCOUNT_ID  = ? " +
            " AND CALC_ID = ? ";

    String MEMORY_CHARGES_SELECT =
            "SELECT " +
            "                   AMOUNT, " +
            "                   WORKSPACE_ID  " +
            "FROM " +
            "  MEMORY_CHARGES " +
            "WHERE " +
            " ACCOUNT_ID  = ? " +
            " AND CALC_ID = ? ";
}
