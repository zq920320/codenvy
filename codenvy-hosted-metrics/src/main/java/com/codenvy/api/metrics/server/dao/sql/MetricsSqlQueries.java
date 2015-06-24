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
package com.codenvy.api.metrics.server.dao.sql;

import java.util.concurrent.TimeUnit;

/**
 * Set of SQL queries
 *
 * @author Sergii Kabashniuk
 */
public interface MetricsSqlQueries {
    /**
     * Multiplier to transform GB/h to MB/msec back and forth.
     */
    long MBMSEC_TO_GBH_MULTIPLIER = TimeUnit.HOURS.toMillis(1) * 1000;

    String GBH_SUM = " SUM(" +
                     "     ROUND(" +
                     "           M.FAMOUNT * ( upper(M.FDURING * ?) - lower(M.FDURING * ?) - 1)/" + MBMSEC_TO_GBH_MULTIPLIER + ".0 " +
                     "           ,6)" +
                     "     ) ";

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

    String METRIC_SELECT_ACCOUNT_GB_WS_TOTAL = "SELECT " +
                                               "  " + GBH_SUM + " AS FAMOUNT, " +
                                               "   M.FWORKSPACE_ID " +
                                               "FROM " +
                                               "  METRICS AS M " +
                                               "WHERE " +
                                               "   M.FACCOUNT_ID=?" +
                                               "   AND M.FDURING && ?" +
                                               "GROUP BY M.FWORKSPACE_ID";

    String METRIC_SELECT_WORKSPACE_GB_TOTAL = "SELECT " +
                                              "  " + GBH_SUM + " AS FAMOUNT, " +
                                              "   M.FWORKSPACE_ID " +
                                              "FROM " +
                                              "  METRICS AS M " +
                                              "WHERE " +
                                              "   M.FWORKSPACE_ID=?" +
                                              "   AND M.FDURING && ?" +
                                              "GROUP BY M.FWORKSPACE_ID";

    String METRIC_UPDATE = "UPDATE  METRICS " +
                           " SET FDURING=? " +
                           " WHERE FID=? ";
}
