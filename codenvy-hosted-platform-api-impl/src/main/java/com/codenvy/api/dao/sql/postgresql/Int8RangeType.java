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
package com.codenvy.api.dao.sql.postgresql;

import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sergii Kabashniuk
 */
public class Int8RangeType extends PGobject {
    /**
     * g1 = ( or ]
     * g2 = from
     * g3 = until
     * g4 = ) or ]
     */
    private final Pattern pattern = Pattern.compile("^(\\(|\\[)\\s*(\\d+)\\s*\\,\\s*(\\d+?)\\s*(\\)|\\])$");
    private final long    from;
    private final long    until;
    private final boolean fromInclusive;
    private final boolean untilInclusive;

    public Int8RangeType(long from, long until, boolean fromInclusive, boolean untilInclusive)
            throws SQLException {
        this.from = from;
        this.until = until;
        this.fromInclusive = fromInclusive;
        this.untilInclusive = untilInclusive;
        setType("int8range");
    }

    public Int8RangeType(PGobject pgObject)
            throws SQLException {

        Matcher matcher = pattern.matcher(pgObject.getValue());
        if (!matcher.matches()) {
            throw new SQLException("Wrong value " + pgObject.getValue());
        }
        from = matcher.group(1).equals("[") ? Long.parseLong(matcher.group(2)) : Long.parseLong(matcher.group(2)) + 1;
        until = matcher.group(4).equals("]") ? Long.parseLong(matcher.group(3)) : Long.parseLong(matcher.group(3)) - 1;
        fromInclusive = true;
        untilInclusive = true;
        setType("int8range");
    }


    public Pattern getPattern() {
        return pattern;
    }

    public long getFrom() {
        return from;
    }

    public long getUntil() {
        return until;
    }

    public boolean isFromInclusive() {
        return fromInclusive;
    }

    public boolean isUntilInclusive() {
        return untilInclusive;
    }

    @Override
    public String getValue() {
        //transform to canonical postgresql form [ from, until)
        StringBuilder value = new StringBuilder();
        value.append("[");
        if (fromInclusive) {
            value.append(from);
        } else {
            value.append(from - 1);
        }
        value.append(" , ");
        if (untilInclusive) {
            value.append(until + 1);
        } else {
            value.append(until);
        }
        value.append(")");
        return value.toString();
    }
}