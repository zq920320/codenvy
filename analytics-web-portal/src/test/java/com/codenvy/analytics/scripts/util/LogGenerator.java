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

package com.codenvy.analytics.scripts.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.UUID;

/** @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a> */
public class LogGenerator {

    /** Generates log file with given events. */
    public static File generateLog(List<Event> events) throws IOException {
        File parent = new File("target", UUID.randomUUID().toString());
        parent.mkdirs();

        File log = new File(parent, UUID.randomUUID().toString());
        log.createNewFile();
        log.deleteOnExit();

        Writer out = new BufferedWriter(new FileWriter(log));

        try {
            for (Event event : events) {
                out.write(event.toString() + "\n");
            }
        } finally {
            out.close();
        }

        return log;
    }

    public static File generateLog(List<Event> events, String dir, String year, String month, String day) throws IOException {
        File parent = new File(dir, year + File.separator + month + File.separator + day);
        parent.mkdirs();

        File log = new File(parent, UUID.randomUUID().toString());
        log.createNewFile();
        log.deleteOnExit();

        Writer out = new BufferedWriter(new FileWriter(log));

        try {
            for (Event event : events) {
                out.write(event.toString() + "\n");
            }
        } finally {
            out.close();
        }

        return log;
    }

}
