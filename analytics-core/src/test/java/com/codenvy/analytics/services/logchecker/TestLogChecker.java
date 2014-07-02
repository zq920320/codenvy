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
package com.codenvy.analytics.services.logchecker;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Injector;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;


/**
 * @author Anatoliy Bazko
 */
public class TestLogChecker extends BaseTest {

    private LogChecker logChecker;

    @BeforeMethod
    public void prepare() throws Exception {
        logChecker = Injector.getInstance(LogChecker.class);
    }

    @Test
    public void testEventChecker() throws Exception {
        Context context = Utils.initializeContext(Parameters.TimeUnit.DAY);

        File reportFile = new File(BASE_DIR, "report.txt");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(reportFile))) {
            logChecker.doEventChecker(context, out);
        }
    }
}
