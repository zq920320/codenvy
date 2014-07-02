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

package com.codenvy.analytics.metrics.accounts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestAbstractAccountMetric extends BaseTest {

    private TestedMetric    metric                = new TestedMetric();
    private List<ValueData> listForPaginationTest = new ArrayList<ValueData>(Arrays.asList(LongValueData.valueOf(1),
                                                                                           LongValueData.valueOf(2),
                                                                                           LongValueData.valueOf(3),
                                                                                           LongValueData.valueOf(4),
                                                                                           LongValueData.valueOf(5)));
    private List<ValueData> listForSortingTest    = new ArrayList<ValueData>(Arrays.asList(MapValueData.valueOf("user=2"),
                                                                                           MapValueData.valueOf("user=3"),
                                                                                           MapValueData.valueOf("user=1")));

    @Test(dataProvider = "paginationContextDataProvider")
    public void testPagination(int page, int perPage, int[] items) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.PAGE, page);
        builder.put(Parameters.PER_PAGE, perPage);

        List<ValueData> result = metric.keepSpecificPage(listForPaginationTest, builder.build());

        assertResult(result, items);
    }

    @Test(dataProvider = "SortingContext")
    public void testSorting(String sort, String[] items) throws Exception {
        Context.Builder builder = new Context.Builder();

        builder.put(Parameters.SORT, sort);
        List<ValueData> result = metric.sort(listForSortingTest, builder.build());

        assertResult(result, items);
    }


    @DataProvider(name = "PaginationContext")
    public static Object[][] paginationContextDataProvider() {
        return new Object[][]{{0, 2, new int[0]},
                              {1, 0, new int[0]},
                              {1, 2, new int[]{1, 2}},
                              {2, 2, new int[]{3, 4}},
                              {3, 2, new int[]{5}},
                              {5, 1, new int[]{5}},
                              {6, 1, new int[0]}};

    }

    @DataProvider(name = "SortingContext")
    public static Object[][] soringContextDataProvider() {
        return new Object[][]{{"+user", new String[]{"1", "2", "3"}},
                              {"-user", new String[]{"3", "2", "1"}}};

    }

    private void assertResult(List<ValueData> result, String[] items) {
        for (int i = 0; i < items.length; i++) {
            MapValueData valueData = (MapValueData)result.get(i);
            assertEquals(valueData.getAll().get("user"), StringValueData.valueOf(items[i]));
        }
    }

    private void assertResult(List<ValueData> result, int[] items) {
        assertEquals(result.size(), items.length);
        for (int i = 0; i < items.length; i++) {
            assertEquals(result.get(i), LongValueData.valueOf(items[i]));
        }
    }


    // -------------------------> Tested metrics

    private class TestedMetric extends AbstractAccountMetric {
        public TestedMetric() {
            super(MetricType.ACCOUNT_USERS_ROLES_LIST);
        }

        @Override
        public ValueData getValue(Context context) throws IOException {
            return null;
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
