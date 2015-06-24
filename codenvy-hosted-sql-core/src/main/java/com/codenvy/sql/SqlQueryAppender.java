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
package com.codenvy.sql;

import com.codenvy.sql.postgresql.Int8RangeType;

import java.sql.SQLException;

import static com.google.common.base.CharMatcher.is;

/**
 * @author Sergii Kabashniuk
 */
public class SqlQueryAppender {

    public static boolean appendEqual(StringBuilder queryBuilder, String fieldName, Object fieldValue) {
        if (fieldValue != null) {
            appendWhereOrAnd(queryBuilder);
            queryBuilder.append(" ").append(fieldName).append(" = ");
            appendValue(queryBuilder, fieldValue);
            return true;
        }
        return false;
    }

    public static boolean appendGreaterOrEqual(StringBuilder queryBuilder, String fieldName, Object fieldValue) {
        if (fieldValue != null) {
            appendWhereOrAnd(queryBuilder);
            queryBuilder.append(" ").append(fieldName).append(" >= ");
            appendValue(queryBuilder, fieldValue);
            return true;
        }
        return false;
    }

    public static boolean appendHavingGreaterOrEqual(StringBuilder queryBuilder, String exp, Object value) {
        if (value != null) {
            appendHavingOrAnd(queryBuilder);
            queryBuilder.append(" ").append(exp).append(" >= ");
            appendValue(queryBuilder, value);
            return true;
        }
        return false;
    }

    public static boolean appendHavingGreater(StringBuilder queryBuilder, String exp, Object value) {
        if (value != null) {
            appendHavingOrAnd(queryBuilder);
            queryBuilder.append(" ").append(exp).append(" > ");
            appendValue(queryBuilder, value);
            return true;
        }
        return false;
    }

    public static boolean appendIsNull(StringBuilder queryBuilder, String fieldName, Object fieldValue) {
        if (fieldValue != null) {
            appendWhereOrAnd(queryBuilder);
            queryBuilder.append(" ").append(fieldName).append(" IS NULL ");
            return true;
        }
        return false;
    }


    public static boolean appendContainsRange(StringBuilder queryBuilder, String fieldName, Long from, Long till)
            throws SQLException {
        if (from != null && till != null) {
            appendWhereOrAnd(queryBuilder);
            queryBuilder.append(" ").append(fieldName).append(" <@ '").append(new Int8RangeType(from,
                                                                                                till,
                                                                                                true,
                                                                                                true)).append("' ");
            return true;
        }
        return false;
    }

    public static boolean appendOverlapRange(StringBuilder queryBuilder, String fieldName, Long from, Long till)
            throws SQLException {
        if (from != null && till != null) {
            appendWhereOrAnd(queryBuilder);
            queryBuilder.append(" ").append(fieldName).append(" && '").append(new Int8RangeType(from,
                                                                                                till,
                                                                                                true,
                                                                                                true)).append("' ");
            return true;
        }
        return false;
    }

    public static boolean appendIsNotNull(StringBuilder queryBuilder, String fieldName, Object fieldValue) {
        if (fieldValue != null) {
            appendWhereOrAnd(queryBuilder);
            queryBuilder.append(" ").append(fieldName).append(" IS NOT NULL ");
            return true;
        }
        return false;
    }

    public static boolean appendLessOrEqual(StringBuilder queryBuilder, String fieldName, Object fieldValue) {
        if (fieldValue != null) {
            appendWhereOrAnd(queryBuilder);
            queryBuilder.append(" ").append(fieldName).append(" <= ");
            appendValue(queryBuilder, fieldValue);
            return true;
        }
        return false;
    }

    public static boolean appendIn(StringBuilder queryBuilder, String fieldName, Object[] fieldValue) {
        if (fieldValue != null && fieldValue.length > 0) {
            appendWhereOrAnd(queryBuilder);
            queryBuilder.append(" ").append(fieldName).append(" IN (");
            for (int i = 0; i < fieldValue.length; i++) {
                appendValue(queryBuilder, fieldValue[i]);
                if (i < fieldValue.length - 1) {
                    queryBuilder.append(", ");
                }
            }

            queryBuilder.append(")");
            return true;
        }
        return false;
    }

    public static void appendWhereOrAnd(StringBuilder queryBuilder) {
        if (queryBuilder.indexOf("WHERE") == -1) {
            queryBuilder.append(" WHERE ");
        } else {
            String whereTail = queryBuilder.substring(queryBuilder.lastIndexOf("WHERE"));
            if (is('(').countIn(whereTail) + is('[').countIn(whereTail) < is(')').countIn(whereTail)) {
                queryBuilder.append(" WHERE ");
            }
        }
        if (queryBuilder.lastIndexOf("WHERE") != queryBuilder.length() - 6) {
            queryBuilder.append(" AND ");
        }
    }

    public static void appendHavingOrAnd(StringBuilder queryBuilder) {

        if (queryBuilder.indexOf("HAVING") == -1) {
            queryBuilder.append(" HAVING ");
        } else {
            String havingTail = queryBuilder.substring(queryBuilder.lastIndexOf("HAVING"));
            //we have HAVING in subquery
            if (is('(').countIn(havingTail) < is(')').countIn(havingTail)) {
                queryBuilder.append(" HAVING ");
            }
        }
        if (queryBuilder.lastIndexOf("HAVING") != queryBuilder.length() - 7) {
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
