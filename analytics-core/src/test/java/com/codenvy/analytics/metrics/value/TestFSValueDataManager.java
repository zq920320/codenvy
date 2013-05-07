/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertFalse;

import com.codenvy.analytics.BaseTest;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.UUID;

import org.testng.annotations.Test;

import com.codenvy.analytics.metrics.MetricType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestFSValueDataManager extends BaseTest {

    @Test(expectedExceptions = FileNotFoundException.class)
    public void shouldThrowExceptionIfFileNotExist() throws Exception {
        File file = new File(BASE_DIR, UUID.randomUUID().toString());

        FSValueDataManager valueManager = spy(new FSValueDataManager(MetricType.BUILT_PROJECTS_NUMBER));
        doReturn(file).when(valueManager).getFile(uuid);

        StringValueData valueData = new StringValueData("test");
        valueManager.store(valueData, uuid);

        file.delete();
        assertFalse(file.exists());

        valueManager.load(uuid);
    }

    @Test
    public void shouldStoreValueIfFileExist() throws Exception {
        File file = new File(BASE_DIR, UUID.randomUUID().toString());
        file.createNewFile();

        FSValueDataManager valueManager = spy(new FSValueDataManager(MetricType.BUILT_PROJECTS_NUMBER));
        doReturn(file).when(valueManager).getFile(uuid);

        valueManager.store(new StringValueData("test"), uuid);
    }
}
