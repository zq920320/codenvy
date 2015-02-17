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

import com.codenvy.api.dao.sql.postgresql.Int8RangeType;

import java.sql.SQLException;

/**
 * @author Sergii Kabashniuk
 */
public class SqlQueryAppender {

    public static void appendEqual(StringBuilder queryBuilder, String fieldName, Object fieldValue) {
        if (fieldValue != null) {
            appendWhereOrAnd(queryBuilder);
            queryBuilder.append(" ").append(fieldName).append(" = ");
            appendValue(queryBuilder, fieldValue);
        }
    }

    public static void appendGreaterOrEqual(StringBuilder queryBuilder, String fieldName, Object fieldValue) {
        if (fieldValue != null) {
            appendWhereOrAnd(queryBuilder);
            queryBuilder.append(" ").append(fieldName).append(" >= ");
            appendValue(queryBuilder, fieldValue);
        }
    }

    public static void appendIsNull(StringBuilder queryBuilder, String fieldName, Object fieldValue) {
        if (fieldValue != null) {
            appendWhereOrAnd(queryBuilder);
            queryBuilder.append(" ").append(fieldName).append(" IS NULL ");
        }
    }


    public static void appendContainsRange(StringBuilder queryBuilder, String fieldName, Long from, Long till)
            throws SQLException {
        if (from != null && till != null) {
            appendWhereOrAnd(queryBuilder);
            queryBuilder.append(" ").append(fieldName).append(" <@ ").append(new Int8RangeType(from,
                                                                                               till,
                                                                                               true,
                                                                                               true));
        }
    }

    public static void appendIsNotNull(StringBuilder queryBuilder, String fieldName, Object fieldValue) {
        if (fieldValue != null) {
            appendWhereOrAnd(queryBuilder);
            queryBuilder.append(" ").append(fieldName).append(" IS NOT NULL ");
        }
    }

    public static void appendLessOrEqual(StringBuilder queryBuilder, String fieldName, Object fieldValue) {
        if (fieldValue != null) {
            appendWhereOrAnd(queryBuilder);
            queryBuilder.append(" ").append(fieldName).append(" >= ");
            appendValue(queryBuilder, fieldValue);
        }
    }

    public static void appendIn(StringBuilder queryBuilder, String fieldName, Object[] fieldValue) {
        if (fieldValue != null && fieldValue.length > 0) {
            appendWhereOrAnd(queryBuilder);
            queryBuilder.append(" ").append(fieldName).append(" IN  (");
            for (int i = 0; i < fieldValue.length; i++) {
                appendValue(queryBuilder, fieldValue[i]);
                if (i < fieldValue.length - 1) {
                    queryBuilder.append(", ");
                }
            }

            queryBuilder.append(" ) ");
        }
    }

    public static void appendWhereOrAnd(StringBuilder queryBuilder) {
        if (queryBuilder.indexOf("WHERE") == -1) {
            queryBuilder.append(" WHERE ");
        }
        if (queryBuilder.lastIndexOf("WHERE") != queryBuilder.length() - 6) {
            queryBuilder.append(" AND ");
        }
    }

    public static void appendValue(StringBuilder queryBuilder, Object fieldValue) {
        if (fieldValue instanceof String) {
            queryBuilder.append(" '").append(fieldValue).append("' ");
        } else {
            queryBuilder.append(" ").append(fieldValue).append(" ");
        }
    }


    public static void appendLimit(StringBuilder queryBuilder, Integer limit) {
        if (limit != null && limit > 0) {
            queryBuilder.append(" LIMIT ").append(limit);
        }
    }


    public static void appendOffset(StringBuilder queryBuilder, Integer offset) {
        if (offset != null && offset > 0) {
            queryBuilder.append(" OFFSET  ").append(offset);
        }
    }
}
