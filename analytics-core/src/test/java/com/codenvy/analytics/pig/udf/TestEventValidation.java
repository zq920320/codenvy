/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.pig.udf;

import com.codenvy.analytics.BaseTest;

import org.apache.pig.data.Tuple;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Alexander Reshetnyak
 */
public class TestEventValidation extends BaseTest {

    EventValidation eventValidation;

    @BeforeClass
    public void prepare() throws Exception {
        eventValidation = new EventValidation();
    }

    @Test(dataProvider = "providerTrue")
    public void shoulTruedEventValidation(String event, String ws, String user, String message) throws Exception {
        Tuple tuple = makeTuple(event, ws, user, message);
        assertEquals(eventValidation.exec(tuple).booleanValue(), true);
    }

    @Test(dataProvider = "providerFalse")
    public void shouldFalseEventValidation(String event, String ws, String user, String message) throws Exception {
        Tuple tuple = makeTuple(event, ws, user, message);
        assertEquals(eventValidation.exec(tuple).booleanValue(), false);
    }

    private Tuple makeTuple(String event, String ws, String user, String message) {
        Tuple tuple = tupleFactory.newTuple();
        tuple.append(event);
        tuple.append(ws);
        tuple.append(user);
        tuple.append(message);

        return tuple;
    }

    @DataProvider(name = "providerTrue")
    public Object[][] createDataTrue() {
        return new Object[][]{
                {"project-created", "ws1", "user1", "127.0.0.1 2013-12-05 01:26:30,878[]  [] []    [][][] - EVENT#project-created# PROJECT#Sample-TwitterBootstrap# TYPE#JavaScript#"},
                {"user-created", "ws2", "user2", "127.0.0.1 2013-12-05 01:09:31,323[]  [] []     [][][] - EVENT#user-created# USER-ID#usermwl9896s2we14h9n# ALIASES#anonymoususer_zz31bd#"},
                {"user-update-profile", "ws9", "user_tt", "127.0.0.1 2013-12-05 00:12:28,875[]  [] []    [][][] - EVENT#user-update-profile# USER#user_tt# FIRSTNAME## LASTNAME#_l# COMPANY## PHONE## JOBTITLE##"},
                {"project-built", "ws7", "user5", "127.0.0.1 2013-12-05 00:27:57,325[]  [] []  [][] - EVENT#project-built# PROJECT#Default# TYPE#Servlet/JSP#"},
                {"project-built", "ws8", "user6", "127.0.0.1 2013-12-05 00:27:57,325[]  [] []  [][] - EVENT#project-built# PROJECT#PizzaHome-660# TYPE#Default#"},
        };
    }

    @DataProvider(name = "providerFalse")
    public Object[][] createDataFalse() {
        return new Object[][]{
                {"project-created", "ws1", "user1", "127.0.0.1 2013-12-05 01:26:30,878[]  [] []    [][][] - EVENT#project-created# PROJECT#Sample-TwitterBootstrap# TYPE##"},
                {"user-created", "ws2", "user2", "127.0.0.1 2013-12-05 01:09:31,323[]  [] []     [][][] - EVENT#user-created# USER-ID## ALIASES#anonymoususer_zz31bd#"},
                {"project-created", "ws3", "", "127.0.0.1 2013-12-05 01:26:30,878[]  [] []    [][][] - EVENT#project-created# PROJECT#Sample-TwitterBootstrap# TYPE#JavaScript#"},
                {"project-built", "", "user4", "127.0.0.1 2013-12-05 00:27:57,325[]  [] []  [][] - EVENT#project-built# PROJECT#PizzaHome-660# TYPE#Servlet/JSP#"},
                {"project-built", "null", "user4", "127.0.0.1 2013-12-05 00:27:57,325[]  [] []  [][] - EVENT#project-built# PROJECT#PizzaHome-660# TYPE#Servlet/JSP#"},
                {"project-built", "ws4", "null", "127.0.0.1 2013-12-05 00:27:57,325[]  [] []  [][] - EVENT#project-built# PROJECT#PizzaHome-660# TYPE#Servlet/JSP#"},
                {"project-built", "Null", "user4", "127.0.0.1 2013-12-05 00:27:57,325[]  [] []  [][] - EVENT#project-built# PROJECT#PizzaHome-660# TYPE#Servlet/JSP#"},
                {"project-built", "ws4", "NULL", "127.0.0.1 2013-12-05 00:27:57,325[]  [] []  [][] - EVENT#project-built# PROJECT#PizzaHome-660# TYPE#Servlet/JSP#"},
                {"project-built", "ws5", "user5", "127.0.0.1 2013-12-05 00:27:57,325[]  [] []  [][] - EVENT#project-built# PROJECT#null# TYPE#Servlet/JSP#"},
                {"project-built", "ws5", "user5", "127.0.0.1 2013-12-05 00:27:57,325[]  [] []  [][] - EVENT#project-built# PROJECT#Null# TYPE#Servlet/JSP#"},
                {"project-built", "ws6", "user6", "127.0.0.1 2013-12-05 00:27:57,325[]  [] []  [][] - EVENT#project-built# PROJECT#PizzaHome-660# TYPE#NULL#"},
                {"project-built", "default", "user5", "127.0.0.1 2013-12-05 00:27:57,325[]  [] []  [][] - EVENT#project-built# PROJECT#Null# TYPE#Servlet/JSP#"},
                {"project-built", "ws6", "Default", "127.0.0.1 2013-12-05 00:27:57,325[]  [] []  [][] - EVENT#project-built# PROJECT#PizzaHome-660# TYPE#NULL#"},
                {"user-update-profile", "ws9", "", "127.0.0.1 2013-12-05 00:12:28,875[]  [] []    [][][] - EVENT#user-update-profile# USER## FIRSTNAME## LASTNAME## COMPANY## PHONE## JOBTITLE##"}

        };
    }
}
