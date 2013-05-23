/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricType;

import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestFSValueDataManager extends BaseTest {

    @Test(expectedExceptions = FileNotFoundException.class)
    public void shouldThrowExceptionIfFileNotExist() throws Exception {
        FSValueDataManager.store(new LongValueData(2), MetricType.ACTIVE_PROJECTS_NUMBER, uuid);

        File file = FSValueDataManager.getFile(MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
        file.delete();

        assertFalse(file.exists());

        FSValueDataManager.load(MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
    }

    @Test
    public void shouldStoreValueIfFileExist() throws Exception {
        File file = FSValueDataManager.getFile(MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
        file.createNewFile();

        assertTrue(file.exists());

        FSValueDataManager.store(new LongValueData(2), MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
    }
}
