/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
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
package com.codenvy.analytics.pig;

import com.codenvy.analytics.BaseTest;

import org.apache.pig.data.Tuple;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestGetQueryValue extends BaseTest {

    GetQueryValue cutQueryParam;

    @BeforeClass
    public void prepare() throws Exception {
        cutQueryParam = new GetQueryValue();
    }

    @Test(dataProvider = "provider")
    public void shouldCutPTypeParam(String url, String cutUrl) throws Exception {
        Tuple tuple = makeTuple(url, "ptype");
        assertEquals(cutQueryParam.exec(tuple), cutUrl);

    }

    private Tuple makeTuple(String url, String param) {
        Tuple tuple = tupleFactory.newTuple();
        tuple.append(url);
        tuple.append(param);

        return tuple;
    }

    @DataProvider(name = "provider")
    public Object[][] createData() {
        return new Object[][]{
                {"aaa", ""},
                {"bbb&ptype=Type", "Type"},
                {"bbb&ptype=Type&ccc", "Type"},
        };
    }
}
