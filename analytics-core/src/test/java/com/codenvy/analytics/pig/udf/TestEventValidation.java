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
import static org.testng.AssertJUnit.assertNull;

/**
 * @author Alexander Reshetnyak
 * @author Anatoliy Bazko
 */
public class TestEventValidation extends BaseTest {

    EventValidation eventValidation;

    @BeforeClass
    public void prepare() throws Exception {
        eventValidation = new EventValidation();
    }

    @Test(dataProvider = "correctMessagesProvider")
    public void testCorrectMessages(String event, String ws, String user, String message) throws Exception {
        Tuple tuple = makeTuple(event, ws, user, message);
        assertNull(message, eventValidation.exec(tuple));
    }

    @Test(dataProvider = "wrongWorkspacesNamesProvider")
    public void testMessagesWithWrongWorkspaceName(String event, String ws, String user, String message) throws Exception {
        Tuple tuple = makeTuple(event, ws, user, message);
        assertEquals(message, EventValidation.WORKSPACE_IS_EMPTY, eventValidation.exec(tuple));
    }


    @Test(dataProvider = "wrongUsersNamesProvider")
    public void testMessagesWithWrongUserName(String event, String ws, String user, String message) throws Exception {
        Tuple tuple = makeTuple(event, ws, user, message);
        assertEquals(message, EventValidation.USER_IS_EMPTY, eventValidation.exec(tuple));
    }

    @Test(dataProvider = "wrongProjectTypesProvider")
    public void testMessagesWithWrongProjectType(String event, String ws, String user, String message, String type) throws Exception {
        Tuple tuple = makeTuple(event, ws, user, message);
        assertEquals(message,
                     String.format(EventValidation.VALUE_IS_NOT_ALLOWED, type, "TYPE"),
                     eventValidation.exec(tuple));
    }


    @Test(dataProvider = "wrongPaasProvider")
    public void testMessagesWithWrongPaas(String event, String ws, String user, String message, String paas) throws Exception {
        Tuple tuple = makeTuple(event, ws, user, message);
        assertEquals(message,
                     String.format(EventValidation.VALUE_IS_NOT_ALLOWED, paas, "PAAS"),
                     eventValidation.exec(tuple));
    }

    @Test(dataProvider = "wrongUsingParameterProvider")
    public void testMessagesWithWrongUsingParameter(String event, String ws, String user, String message, String value) throws Exception {
        Tuple tuple = makeTuple(event, ws, user, message);
        assertEquals(message,
                     String.format(EventValidation.VALUE_IS_NOT_ALLOWED, value, "USING"),
                     eventValidation.exec(tuple));
    }

    @Test(dataProvider = "correctUsingParameterProvider")
    public void testMessagesWithCorrectUsingParameter(String event, String ws, String user, String message) throws Exception {
        Tuple tuple = makeTuple(event, ws, user, message);
        assertNull(eventValidation.exec(tuple));
    }

    @Test(dataProvider = "correctPaasProvider")
    public void testMessagesWithCorrectPaas(String event, String ws, String user, String message) throws Exception {
        Tuple tuple = makeTuple(event, ws, user, message);
        assertNull(eventValidation.exec(tuple));
    }

    private Tuple makeTuple(String event, String ws, String user, String message) {
        Tuple tuple = tupleFactory.newTuple();
        tuple.append(event);
        tuple.append(ws);
        tuple.append(user);
        tuple.append(message);

        return tuple;
    }

    @DataProvider(name = "correctMessagesProvider")
    public Object[][] getCorrectMessages() {
        return new Object[][]{
                {"project-created", "ws", "user", "PROJECT#Sample-TwitterBootstrap# TYPE#JavaScript# PAAS#null#"},
                {"factory-created", "ws", "user", "PROJECT#project# TYPE## REPO-URL#repo# FACTORY-URL#factory# AFFILIATE-ID## ORG-ID##"},
                {"user-created", "ws", "user", "USER-ID#usermwl9896s2we14h9n# EMAILS#anonymoususer_zz31bd#"},
                {"user-update-profile", "ws", "user", "USER#user_tt# FIRSTNAME## LASTNAME#_l# COMPANY## PHONE## JOBTITLE## EMAILS#user# USER-ID#id#"},
                {"project-built", "ws", "user", "PROJECT#Default# TYPE#Servlet/JSP#"},
                {"project-built", "ws", "user", "PROJECT#PizzaHome-660# TYPE#Default#"},
        };
    }

    @DataProvider(name = "wrongWorkspacesNamesProvider")
    public Object[][] getMessagesWithWrongWorkspaceName() {
        return new Object[][]{
                {"project-built", "", "user", "PROJECT#PizzaHome-660# TYPE#Servlet/JSP#"},
                {"project-built", "null", "user", "PROJECT#PizzaHome-660# TYPE#Servlet/JSP#"},
                {"project-built", "default", "user", "PROJECT#PizzaHome-660# TYPE#Servlet/JSP#"},
                {"project-built", null, "user", "PROJECT#PizzaHome-660# TYPE#Servlet/JSP#"},
        };
    }

    @DataProvider(name = "wrongUsersNamesProvider")
    public Object[][] getMessagesWithWrongUserName() {
        return new Object[][]{
                {"project-created", "ws", "", "PROJECT#Sample-TwitterBootstrap# TYPE#JavaScript# PAAS#null#"},
                {"project-created", "ws", "null", "PROJECT#Sample-TwitterBootstrap# TYPE#JavaScript# PAAS#null#"},
                {"project-created", "ws", "default", "PROJECT#Sample-TwitterBootstrap# TYPE#JavaScript# PAAS#null#"},
                {"project-created", "ws", null, "PROJECT#Sample-TwitterBootstrap# TYPE#JavaScript# PAAS#null#"}
        };
    }

    @DataProvider(name = "wrongProjectTypesProvider")
    public Object[][] getMessagesWithWrongProjectType() {
        return new Object[][]{
                {"project-created", "ws", "user", "PROJECT#Sample-TwitterBootstrap# TYPE## PAAS#null#", ""},
                {"project-created", "ws", "user", "PROJECT#Sample-TwitterBootstrap# PAAS#null#", "null"},
                {"project-created", "ws", "user", "PROJECT#Sample-TwitterBootstrap# TYPE#unknown# PAAS#null#", "unknown"},
                {"project-created", "ws", "user", "PROJECT#Sample-TwitterBootstrap# TYPE#null# PAAS#null#", "null"},
        };
    }

    @DataProvider(name = "wrongPaasProvider")
    public Object[][] getMessagesWithWrongPaas() {
        return new Object[][]{
                {"application-created", "ws", "user", "PROJECT#Sample# TYPE#JavaScript#", "null"},
                {"application-created", "ws", "user", "PROJECT#Sample# TYPE#JavaScript# PAAS##", ""},
                {"application-created", "ws", "user", "PROJECT#Sample# TYPE#JavaScript# PAAS#default#", "default"},
                {"application-created", "ws", "user", "PROJECT#Sample# TYPE#JavaScript# PAAS#null#", "null"},
                {"application-created", "ws", "user", "PROJECT#Sample# TYPE#JavaScript# PAAS#unknown#", "unknown"},
                {"application-created", "ws", "user", "PROJECT#Sample# TYPE#JavaScript# PAAS#LOCAL#", "LOCAL"},
        };
    }

    @DataProvider(name = "correctPaasProvider")
    public Object[][] getMessagesWithCorrectPaas() {
        return new Object[][]{
                {"application-created", "ws", "user", "PROJECT#Sample# TYPE#JavaScript# PAAS#gae#"},
                {"application-created", "ws", "user", "PROJECT#Sample# TYPE#JavaScript# PAAS#CloudFoundry#"},
                {"application-created", "ws", "user", "PROJECT#Sample# TYPE#JavaScript# PAAS#AWS#"},
                {"project-deployed", "ws", "user", "PROJECT#Sample# TYPE#JavaScript# PAAS#LOCAL#"},
                {"artifact-deployed", "ws", "user", "PROJECT#Sample# TYPE#JavaScript# PAAS#LOCAL#"},
        };
    }

    @DataProvider(name = "wrongUsingParameterProvider")
    public Object[][] getMessagesWithWrongUsingParameter() {
        return new Object[][]{
                {"user-sso-logged-in", "default", "user", "USING#null#", "null"},
                {"user-sso-logged-in", "default", "user", "USING##", ""},
                {"user-sso-logged-in", "default", "user", "", "null"},
        };
    }

    @DataProvider(name = "correctUsingParameterProvider")
    public Object[][] getMessagesWithCorrectUsingParameter() {
        return new Object[][]{
                {"user-sso-logged-in", "default", "user", "USING#sysldap#"},
                {"user-sso-logged-in", "default", "user", "USING#org#"},
        };
    }
}
