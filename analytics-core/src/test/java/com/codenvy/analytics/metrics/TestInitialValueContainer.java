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


package com.codenvy.analytics.metrics;


import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Injector;
import com.codenvy.analytics.datamodel.LongValueData;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.ParseException;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestInitialValueContainer extends BaseTest {

    private InitialValueContainer initialValueContainer;

    @BeforeClass
    public void prepare() {
        initialValueContainer = Injector.getInstance(InitialValueContainer.class);
    }

    @Test
    public void testInitialValue() throws Exception {
        assertEquals(new LongValueData(10), initialValueContainer.getInitialValue("total_workspaces"));
        assertEquals(new LongValueData(20), initialValueContainer.getInitialValue("total_users"));
        assertEquals(new LongValueData(30), initialValueContainer.getInitialValue("total_projects"));
    }

    @Test
    public void shouldThrowExceptionIfMetricUnknown() throws ParseException {
        assertNull(initialValueContainer.getInitialValue("bla-bla"));
    }

    @Test(expectedExceptions = InitialValueNotFoundException.class)
    public void shouldThrowExceptionIfToDateBefore() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20111231");

        initialValueContainer.validateExistenceInitialValueBefore(builder.build());
    }

    @Test
    public void shouldNotThrowExceptionIfToDateEquals() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "201120101");
        builder.put(Parameters.FROM_DATE, "201120101");

        initialValueContainer.validateExistenceInitialValueBefore(builder.build());
    }

    @Test
    public void shouldNotThrowExceptionIfToDateAfter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "201120102");
        builder.put(Parameters.FROM_DATE, "201120102");

        initialValueContainer.validateExistenceInitialValueBefore(builder.build());
    }
}
