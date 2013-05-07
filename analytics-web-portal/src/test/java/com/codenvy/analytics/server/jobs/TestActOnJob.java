/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.jobs;

import org.testng.annotations.Test;

import java.io.File;
import java.util.Calendar;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestActOnJob {

    @Test
    public void testPrepareFile() throws Exception {
        File base = new File("target");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        ActOnJob job = new ActOnJob();
    }
}
