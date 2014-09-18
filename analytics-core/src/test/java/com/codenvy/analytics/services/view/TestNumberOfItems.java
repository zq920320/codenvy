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
package com.codenvy.analytics.services.view;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Injector;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.CollectionValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.services.pig.PigRunner;
import com.google.common.io.ByteStreams;
import com.google.common.io.OutputSupplier;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.testng.Assert.assertEquals;

/** @author Anatoliy Bazko */
public class TestNumberOfItems extends BaseTest {

    private PigRunner pigRunner;

    private static final String BASE_TEST_RESOURCE_DIR  = BASE_DIR + "/test-classes/" + TestNumberOfItems.class.getSimpleName();
    private static final String TEST_STATISTICS_ARCHIVE = TestNumberOfItems.class.getSimpleName() + "/messages_";

    @BeforeClass
    public void init() throws Exception {
        pigRunner = Injector.getInstance(PigRunner.class);
        runScript();
    }

    private void runScript() throws Exception {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        Context context = Utils.prevDateInterval(new Context.Builder(Utils.initializeContext(Parameters.TimeUnit.DAY).getAll()));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -2);

        context = context.cloneAndPut(Parameters.LOG, getResourceAsBytes("2014-04-22", df.format(calendar.getTime())).getAbsolutePath());
        pigRunner.forceExecute(context);
    }

    private File getResourceAsBytes(String originalDate, String newDate) throws Exception {
        String archive = TestNumberOfItems.class.getClassLoader().getResource(TEST_STATISTICS_ARCHIVE + originalDate).getFile();

        try (ZipInputStream in = new ZipInputStream(new BufferedInputStream(new FileInputStream(archive)))) {
            ZipEntry zipEntry = in.getNextEntry();

            try {
                String name = zipEntry.getName();
                File resource = new File(BASE_TEST_RESOURCE_DIR, name);

                try (OutputStream out = new BufferedOutputStream(new FileOutputStream(resource))) {
                    String resourceAsString = new String(ByteStreams.toByteArray(in), "UTF-8");
                    resourceAsString = resourceAsString.replace(originalDate, newDate);

                    ByteStreams.write(resourceAsString.getBytes("UTF-8"), new OutputSupplier<OutputStream>() {
                        @Override
                        public OutputStream getOutput() throws IOException {
                            return out;
                        }
                    });

                    return resource;
                }
            } finally {
                in.closeEntry();
            }
        }
    }

    @Test
    public void testNumberOfItems() throws Exception {
        assertNumberOfItems(MetricType.USAGE_TIME_BY_WORKSPACES_LIST, MetricType.USAGE_TIME_BY_WORKSPACES);
        assertNumberOfItems(MetricType.USERS_ACTIVITY_LIST, MetricType.USERS_ACTIVITY);
        assertNumberOfItems(MetricType.USAGE_TIME_BY_USERS_LIST, MetricType.USAGE_TIME_BY_USERS);
        assertNumberOfItems(MetricType.WORKSPACES_STATISTICS_LIST, MetricType.WORKSPACES_STATISTICS);
        assertNumberOfItems(MetricType.USERS_STATISTICS_LIST, MetricType.USERS_STATISTICS);
        assertNumberOfItems(MetricType.PRODUCT_USAGE_SESSIONS_LIST, MetricType.PRODUCT_USAGE_SESSIONS);
        assertNumberOfItems(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS);
        assertNumberOfItems(MetricType.CREATED_FACTORIES_SET, MetricType.CREATED_UNIQUE_FACTORIES);
        assertNumberOfItems(MetricType.CREATED_FACTORIES_LIST, MetricType.CREATED_FACTORIES);
        assertNumberOfItems(MetricType.FACTORY_USERS_LIST, MetricType.FACTORY_USERS);
        assertNumberOfItems(MetricType.FACTORY_STATISTICS_LIST, MetricType.FACTORY_STATISTICS);
    }

    private void assertNumberOfItems(MetricType listMetricType, MetricType countMetricType) throws IOException {
        Context context = new Context.Builder().build();

        Metric listMetric = MetricFactory.getMetric(listMetricType);
        Metric countMetric = MetricFactory.getMetric(countMetricType);

        CollectionValueData listValueData = (CollectionValueData)listMetric.getValue(context);
        LongValueData longValueData = (LongValueData)countMetric.getValue(context);

        assertEquals(listValueData.size(), longValueData.getAsLong());
    }
}
