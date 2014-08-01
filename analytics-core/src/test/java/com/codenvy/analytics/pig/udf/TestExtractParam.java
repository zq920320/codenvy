/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
public class TestExtractParam extends BaseTest {

    ExtractParam extractParam;

    @BeforeClass
    public void prepare() throws Exception {
        extractParam = new ExtractParam();
    }

    @Test(dataProvider = "provider")
    public void testUrlDecode(String message, String paramName, String paramValue) throws Exception {
        Tuple tuple = makeTuple(message, paramName);
        assertEquals(paramValue, extractParam.exec(tuple));
    }

    private Tuple makeTuple(String message, String paramName) {
        Tuple tuple = tupleFactory.newTuple();
        tuple.append(message);
        tuple.append(paramName);
        return tuple;
    }

    @DataProvider(name = "provider")
    public Object[][] createData() {
        return new Object[][]{
                {" EVENT#user-created# USER#user1@gmail.com# USER-ID#uid1# EMAILS#user1@gmail.com# ", "EVENT", "user-created"},
                {" EVENT#user-created# USER#user1@gmail.com# USER-ID#uid1# EMAILS#user1@gmail.com# ", "EMAILS", "user1@gmail.com"},
                {" EVENT#user-created# USER#user1@gmail.com# USER-ID#uid1# EMAILS## ", "EMAILS", ""},
                {" EVENT#workspace-created# WS#ws1# WS-ID#wsid1# USER#user1@gmail.com# ", "WS", "ws1"},
                {" EVENT#workspace-created# WS#ws1# WS-ID#wsid1# USER#user1@gmail.com# ", "WS-ID", "wsid1"},
                {" EVENT#session-started# WS#ws1# USER#user1@gmail.com# PARAMETERS#WINDOW=ide,SESSION-ID=sid1# ", "SESSION-ID", "sid1"},
                {" EVENT#session-started# WS#ws1# USER#user1@gmail.com# PARAMETERS#WINDOW=ide,SESSION-ID=sid1,ITEM=run# ", "SESSION-ID", "sid1"},
                {" EVENT#session-started# WS#ws1# USER#user1@gmail.com# PARAMETERS#WINDOW=ide,SESSION-ID=,ITEM=run# ", "SESSION-ID", ""},
                {" EVENT#session-started# WS#ws1# USER#user1@gmail.com# PARAMETERS#WINDOW=ide,SESSION-ID=# ", "SESSION-ID", ""},
                {" EVENT#session-started# WS#ws1# USER#user1@gmail.com# PARAMETERS#WINDOW=ide,SESSION-ID=# ", "WINDOW", "ide"},
                {" EVENT#session-started# WS#ws1# USER#user1@gmail.com# PARAMETERS#WINDOW=,SESSION-ID=# ", "WINDOW", ""},
                {" EVENT#session-started# WS#ws1# USER#user1@gmail.com# PARAMETERS#SESSION-ID=sid1# ", "SESSION-ID", "sid1"},
                {" EVENT#session-started# WS#ws1# USER#user1@gmail.com# PARAMETERS#SESSION-ID=# ", "SESSION-ID", ""},
                {" EVENT#ide-usage# PARAMETERS#p1=%2C,p2=%3D,p3=%23#", "p1", ","},
                {" EVENT#ide-usage# PARAMETERS#p1=%2C,p2=%3D,p3=%23#", "p2", "="},
                {" EVENT#ide-usage# PARAMETERS#p1=%2C,p2=%3D,p3=%23#", "p3", "#"},
                {" EVENT#session-started# WS#ws1# USER#user1@gmail.com# PARAMETERS#SESSION%23ID=sid1# ", "SESSION#ID", "sid1"},
                {" EVENT#session-started# WS#ws1# USER#user1@gmail.com# PARAMETERS#SESSION%23ID=%23sid1# ", "SESSION#ID", "#sid1"},
                {" EVENT#session-started# WS#ws1# USER#user1@gmail.com# PARAMETERS#SESSION%23ID=# ", "SESSION#ID", ""},
                {" EVENT#session-started# WS#ws1# USER#user1@gmail.com# PARAMETERS#WINDOW=ide,SESSION-ID=sid1# ", "SESSION-ID2", null},
                {" EVENT#session-started# WS#ws1# USER#user1@gmail.com# PARAMETERS#WINDOW=ide,SESSION%23ID=sid1# ", "SESSION#ID2", null},
        };
    }
}