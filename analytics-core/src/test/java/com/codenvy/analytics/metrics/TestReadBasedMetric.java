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
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestReadBasedMetric extends BaseTest {

    @Test
    public void testEvaluateValue() throws Exception {
        TestMetric spyMetric = spy(new TestMetric());
        doReturn(new LongValueData(10)).when(spyMetric).loadValue(anyMap());

        assertEquals(spyMetric.getValue(Utils.newContext()), new LongValueData(10L));
    }


    public class TestMetric extends SimpleReadBasedMetric {

        private TestMetric() {
            super("TestReadBasedMetric");
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }

        @Override
        public Set<Parameters> getParams() {
            return new HashSet<>(Arrays.asList(new Parameters[]{Parameters.FROM_DATE, Parameters.TO_DATE}));
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
