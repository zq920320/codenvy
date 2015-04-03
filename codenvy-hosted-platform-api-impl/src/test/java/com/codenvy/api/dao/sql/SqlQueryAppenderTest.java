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

import static com.codenvy.api.dao.sql.SqlQueryAppender.appendHavingGreater;
import static com.codenvy.api.dao.sql.SqlQueryAppender.appendHavingGreaterOrEqual;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import java.sql.SQLException;

/**
 * @author Sergii Kabashniuk
 */
public class SqlQueryAppenderTest {
    @Test
    public void testShouldBeAbleToAppendHavingToQueryWithSubQuery() {
        //given
        StringBuilder query = new StringBuilder("SELECT SUM(K) FROM (SELECT SUM(T) FROM A HAVING SUM(T)>0)");
        //when
        appendHavingGreaterOrEqual(query, "SUM(k)", 5);
        //then
        assertEquals(query.toString(), "SELECT SUM(K) FROM (SELECT SUM(T) FROM A HAVING SUM(T)>0) HAVING  SUM(k) >=  5 ");
    }

    @Test
    public void testShouldBeAbleToAppendHavingToQueryWithSubQueryTwice() {
        //given
        StringBuilder query = new StringBuilder("SELECT SUM(K), P FROM (SELECT SUM(T) FROM A HAVING SUM(T)>0)  GROUP BY P");
        //when
        appendHavingGreaterOrEqual(query, "SUM(k)", 5);
        appendHavingGreater(query, "P", 3);
        //then
        assertEquals(query.toString(),
                     "SELECT SUM(K), P FROM (SELECT SUM(T) FROM A HAVING SUM(T)>0)  GROUP BY P HAVING  SUM(k) >=  5  AND  P >  3 ");
    }

    @Test
    public void testShouldBeAbleToAppendHavingToQueryWithOutSubQuery() {
        //given
        StringBuilder query = new StringBuilder("SELECT SUM(K) FROM T");
        //when
        appendHavingGreaterOrEqual(query, "SUM(k)", 5);
        //then
        assertEquals(query.toString(), "SELECT SUM(K) FROM T HAVING  SUM(k) >=  5 ");
    }

    @Test
    public void testShouldBeAbleToAppendHavingToQueryWithoutSubQueryTwice() {
        //given
        StringBuilder query = new StringBuilder("SELECT SUM(K), P FROM T  GROUP BY P");
        //when
        appendHavingGreaterOrEqual(query, "SUM(k)", 5);
        appendHavingGreater(query, "P", 3);
        //then
        assertEquals(query.toString(), "SELECT SUM(K), P FROM T  GROUP BY P HAVING  SUM(k) >=  5  AND  P >  3 ");
    }


    @Test
    public void testShouldBeAbleToAppendWhereToQueryWithSubQuery() {
        //given
        StringBuilder query = new StringBuilder("SELECT K FROM (SELECT P FROM A WHERE D >0) AS P");
        //when
        SqlQueryAppender.appendEqual(query, "F", 5);
        //then
        assertEquals(query.toString(), "SELECT K FROM (SELECT P FROM A WHERE D >0) AS P WHERE  F =  5 ");
    }

    @Test
    public void testShouldBeAbleToAppendWhereToQueryWithSubQueryTwice() {
        //given
        StringBuilder query = new StringBuilder("SELECT K FROM (SELECT P FROM A WHERE D >0) AS P");
        //when
        SqlQueryAppender.appendLessOrEqual(query, "D", 5);
        SqlQueryAppender.appendGreaterOrEqual(query, "K", 5);
        //then
        assertEquals(query.toString(), "SELECT K FROM (SELECT P FROM A WHERE D >0) AS P WHERE  D <=  5  AND  K >=  5 ");
    }

    @Test
    public void testShouldBeAbleToAppendWhereToQueryWithOutSubQuery() {
        //given
        StringBuilder query = new StringBuilder("SELECT K FROM P");
        //when
        SqlQueryAppender.appendEqual(query, "F", 5);
        //then
        assertEquals(query.toString(), "SELECT K FROM P WHERE  F =  5 ");
    }

    @Test
    public void testShouldBeAbleToAppendWhereToQueryWithoutSubQuery() {
        //given
        StringBuilder query = new StringBuilder("SELECT K FROM P");
        //when
        SqlQueryAppender.appendEqual(query, "D", 5);
        SqlQueryAppender.appendGreaterOrEqual(query, "K", 5);
        SqlQueryAppender.appendIsNull(query, "F", 5);
        //then
        assertEquals(query.toString(), "SELECT K FROM P WHERE  D =  5  AND  K >=  5  AND  F IS NULL ");
    }

    @Test
    public void testShouldBeAbleToAppendWhereToQueryWithoutSubQueryWithRange() throws SQLException {
        //given
        StringBuilder query = new StringBuilder("SELECT K FROM P");
        //when
        SqlQueryAppender.appendEqual(query, "D", 5);
        SqlQueryAppender.appendGreaterOrEqual(query, "K", 5);
        SqlQueryAppender.appendOverlapRange(query, "M", 1L, 2L);
        SqlQueryAppender.appendIsNull(query, "F", 5);
        //then
        assertEquals(query.toString(), "SELECT K FROM P WHERE  D =  5  AND  K >=  5  AND  M && '[1 , 3)'  AND  F IS NULL ");
    }


}
