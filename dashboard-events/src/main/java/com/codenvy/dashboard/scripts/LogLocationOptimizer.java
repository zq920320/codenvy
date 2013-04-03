/*
 *    Copyright (C) 2013 eXo Platform SAS.
 *
 *    This is free software; you can redistribute it and/or modify it
 *    under the terms of the GNU Lesser General Public License as
 *    published by the Free Software Foundation; either version 2.1 of
 *    the License, or (at your option) any later version.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this software; if not, write to the Free
 *    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *    02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.codenvy.dashboard.scripts;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/** Log path optimization utility provides means to parse paths where logs are stored */
public class LogLocationOptimizer {

    public static String generatePathString(String baseDir, String fromDateString, String toDateString) throws ParseException,
                                                                                                       IllegalStateException {
        DateFormat df = new SimpleDateFormat("yyyyMMdd");

        Calendar fromDate = Calendar.getInstance();
        Calendar toDate = Calendar.getInstance();

        fromDate.setTime(df.parse(fromDateString));
        toDate.setTime(df.parse(toDateString));

        return doGeneratePath(baseDir, fromDate, toDate);
    }

    public static String generatePathString(String baseDir, String lastMinutes) throws ParseException, IllegalStateException {
        Calendar fromDate = Calendar.getInstance();
        Calendar toDate = Calendar.getInstance();

        fromDate.add(Calendar.MINUTE, -Integer.valueOf(lastMinutes));

        return doGeneratePath(baseDir, fromDate, toDate);
    }

    /**
     * If particular path is absent it will not be included in resulted set. Since Pig framework throw an exception when path does not
     * exist.
     */
    private static String doGeneratePath(String baseDir, Calendar fromDate, Calendar toDate)
    {
        final DateFormat df = new SimpleDateFormat("yyyy" + File.separator + "MM" + File.separator + "dd");

        if (fromDate.after(toDate))
        {
            throw new IllegalStateException(fromDate.toString() + " is after " + toDate.toString());
        }

        StringBuilder builder = new StringBuilder();
        do {
            File file = new File(baseDir, df.format(fromDate.getTime()));
            if (file.exists())
            {
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
