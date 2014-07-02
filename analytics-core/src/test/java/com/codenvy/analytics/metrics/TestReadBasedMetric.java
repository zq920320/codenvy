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

package com.codenvy.analytics.metrics;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.mongodb.DBObject;

import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestReadBasedMetric extends BaseTest {

    @Test
    public void testEvaluateValue() throws Exception {
        TestMetric spyMetric = spy(new TestMetric());
        doReturn(new LongValueData(10)).when(spyMetric).getValue(any(Context.class));

        assertEquals(spyMetric.getValue(new Context.Builder().build()), new LongValueData(10L));
    }


    public class TestMetric extends ReadBasedMetric {
        private TestMetric() {
            super("TestReadBasedMetric");
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getTrackedFields() {
            return new String[0];
        }

        @Override
        public DBObject[] getSpecificDBOperations(Context clauses) {
            return new DBObject[0];
        }
    }
}
