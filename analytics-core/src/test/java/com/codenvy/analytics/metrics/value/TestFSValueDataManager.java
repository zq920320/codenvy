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


package com.codenvy.analytics.metrics.value;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricType;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestFSValueDataManager extends BaseTest {

    @Test(expectedExceptions = FileNotFoundException.class)
    public void shouldThrowExceptionIfFileNotExist() throws Exception {
        FSValueDataManager.store(new LongValueData(2), MetricType.ACTIVE_USERS, uuid);

        File file = FSValueDataManager.getFile(MetricType.ACTIVE_USERS, uuid);
        file.delete();

        assertFalse(file.exists());

        FSValueDataManager.load(MetricType.ACTIVE_USERS, uuid);
    }

    @Test
    public void shouldStoreValueIfFileExist() throws Exception {
        File file = FSValueDataManager.getFile(MetricType.ACTIVE_USERS, uuid);
        file.createNewFile();

        assertTrue(file.exists());

        FSValueDataManager.store(new LongValueData(2), MetricType.ACTIVE_USERS, uuid);
    }
}
