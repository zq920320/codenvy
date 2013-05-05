/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.scripts.executor.pig;


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.codenvy.analytics.metrics.Utils;

/** Log path optimization utility provides means to parse paths where logs are stored */
public class LogLocationOptimizer {

    public static String generatePaths(String baseDir, String fromDateString, String toDateString) throws ParseException,
                                                                                                  IllegalStateException, IOException {
        Calendar fromDate = Utils.parseDate(fromDateString);
        Calendar toDate = Utils.parseDate(toDateString);

        return doGeneratePath(baseDir, fromDate, toDate);
    }

    public static String generatePaths(String baseDir, String lastMinutes) throws ParseException, IllegalStateException {
        Calendar fromDate = Calendar.getInstance();
        Calendar toDate = Calendar.getInstance();

        fromDate.add(Calendar.MINUTE, -Integer.valueOf(lastMinutes));

        return doGeneratePath(baseDir, fromDate, toDate);
    }

    /**
     * If particular path is absent it will not be included in resulted set. Since Pig framework throw an exception when path does not
     * exist.
     */
    private static String doGeneratePath(String baseDir, Calendar fromDate, Calendar toDate) {
        final DateFormat df = new SimpleDateFormat("yyyy" + File.separator + "MM" + File.separator + "dd");

        if (fromDate.after(toDate)) {
            throw new IllegalStateException(Utils.formatDate(fromDate) + " is after " + Utils.formatDate(toDate));
        }

        StringBuilder builder = new StringBuilder();
        do {
            File file = new File(baseDir, df.format(fromDate.getTime()));
            if (file.exists()) {
                builder.append(file.getPath());
                builder.append(',');
            }

            fromDate.add(Calendar.DAY_OF_MONTH, 1);
        } while (!fromDate.after(toDate));

        if (builder.length() != 0) {
            builder.deleteCharAt(builder.length() - 1);
        }

        return builder.toString();
    }
}
