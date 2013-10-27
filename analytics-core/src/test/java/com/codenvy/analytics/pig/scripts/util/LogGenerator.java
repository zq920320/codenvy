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

package com.codenvy.analytics.pig.scripts.util;

import com.codenvy.analytics.BaseTest;

import java.io.*;
import java.util.List;
import java.util.UUID;

/** @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a> */
public class LogGenerator {

    /** Generates log file with given events. */
    public static File generateLog(List<Event> events) throws IOException {
        File log = getFile();

        try (Writer out = new BufferedWriter(new FileWriter(log))) {
            try {
                for (Event event : events) {
                    out.write(event.toString() + "\n");
                }
            } finally {
                out.close();
            }
        }

        return log;
    }

    /** Generates log file with given strings. */
    public static File generateLogByStrings(List<String> strings) throws IOException {
        File log = getFile();

        try (Writer out = new BufferedWriter(new FileWriter(log))) {
            try {
                for (String string : strings) {
                    out.write(string + "\n");
                }
            } finally {
                out.close();
            }
        }

        return log;
    }

    private static File getFile() throws IOException {
        File parent = new File(BaseTest.BASE_DIR, UUID.randomUUID().toString());
        if (!parent.mkdirs()) {
            throw new IOException("Can't create directory tree " + parent.getPath());
        }


        File log = new File(parent, UUID.randomUUID().toString());
        if (!log.createNewFile()) {
            throw new IOException("The file can't be created " + log.getPath());
        }
        log.deleteOnExit();

        return log;
    }
}
