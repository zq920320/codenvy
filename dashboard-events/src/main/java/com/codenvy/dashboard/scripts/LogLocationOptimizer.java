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
import java.util.LinkedList;
import java.util.List;

/** Log path optimization utility provides means to parse paths where logs are stored */
public class LogLocationOptimizer {
    public static List<String> generatePathList(String baseDir, String fromDateString, String toDateString) {
        LoggingDate fromDate = new LoggingDate(fromDateString);
        LoggingDate toDate = new LoggingDate(toDateString);

        List<String> paths = new LinkedList<String>();

        while (fromDate.equalOrLess(toDate)) {
            paths.add(baseDir + File.separator + fromDate.toString());
            fromDate.increment();
        }

        return paths;
    }

    public static String generatePathString(String baseDir, String fromDateString, String toDateString) {

        List<String> paths = generatePathList(baseDir, fromDateString, toDateString);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paths.size(); i++) {
            sb.append(paths.get(i));
            sb.append(", ");
        }
        sb.delete(sb.lastIndexOf(","), sb.lastIndexOf(",") + 2);

        return sb.toString();
    }
}

class LoggingDate {
    int year;

    int month;

    int day;

    LoggingDate(String date) {
        this.year = Integer.parseInt(date.substring(0, 4));
        this.month = Integer.parseInt(date.substring(4, 6));
        this.day = Integer.parseInt(date.substring(6, 8));
    }

    public boolean equalOrLess(String cmpDateString) {
        return equalOrLess(new LoggingDate(cmpDateString));
    }

    public boolean equalOrLess(LoggingDate cmpDate) {
        if (year < cmpDate.getYear()) {
            return true;
        } else if (year == cmpDate.getYear()) {
            if (month < cmpDate.getMonth()) {
                return true;
            } else if (month == cmpDate.getMonth()) {
                return day <= cmpDate.getDay();
            }
        }

        return false;
    }

    public void increment() {
        if ((month == 1 && day == 31) ||
            (month == 2 && day == 28) ||
            (month == 3 && day == 31) ||
            (month == 4 && day == 30) ||
            (month == 5 && day == 31) ||
            (month == 6 && day == 30) ||
            (month == 7 && day == 31) ||
            (month == 8 && day == 31) ||
            (month == 9 && day == 30) ||
            (month == 10 && day == 31) ||
            (month == 11 && day == 30) ||
            (month == 12 && day == 31)) {
            day = 1;
            if (month == 12) {
                month = 1;
                year++;
            } else {
                month++;
            }
        } else {
            day++;
        }
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String toString() {
        return String.valueOf(year) + File.separator + addZeroIfNeeded(month) + File.separator + addZeroIfNeeded(day);
    }

    private String addZeroIfNeeded(int number) {
        if (number < 10) {
            return 0 + String.valueOf(number);
        }

        return String.valueOf(number);
    }
}
