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
package com.codenvy.analytics.scripts.executor.pig;

import static org.junit.Assert.assertEquals;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricParameter;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestLogLocationOptimizer extends BaseTest {

    private String baseDir;

    @BeforeMethod
    public void setUp() {
        baseDir = BASE_DIR + File.separator + UUID.randomUUID();
    }

    @Test
    public void testMonth1() throws Exception {
        generateDirs("20130930", "20131101");

        File dir1 = new File(baseDir, "2013" + File.separator + "09" + File.separator + "30");
        File dir2 = new File(baseDir, "2013" + File.separator + "10");
        File dir3 = new File(baseDir, "2013" + File.separator + "11" + File.separator + "01");

        assertEquals(dir1.getPath() + "," + dir2.getPath() + "," + dir3.getPath(),
                     LogLocationOptimizer.generatePaths(baseDir, "20130930", "20131101"));
    }

    @Test
    public void testMonth2() throws Exception {
        generateDirs("20130901", "20130930");

        File dir1 = new File(baseDir, "2013" + File.separator + "09");

        assertEquals(dir1.getPath(), LogLocationOptimizer.generatePaths(baseDir, "20130901", "20130930"));
    }

    @Test
    public void testPathsStringGenerationByDate() throws Exception {
        File dir1 = new File(baseDir, "2013" + File.separator + "12" + File.separator + "31");
        File dir2 = new File(baseDir, "2014" + File.separator + "01" + File.separator + "03");

        dir1.mkdirs();
        dir2.mkdirs();

        assertEquals(dir1.getPath() + "," + dir2.getPath(), LogLocationOptimizer.generatePaths(baseDir, "20131231", "20140103"));
    }

    @Test
    public void testPathsStringGenerationByDateSameDates() throws Exception {
        File dir1 = new File(baseDir, "2013" + File.separator + "12" + File.separator + "31");
        dir1.mkdirs();

        assertEquals(dir1.getPath(), LogLocationOptimizer.generatePaths(baseDir, "20131231", "20131231"));
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testPathsStringGenerationByDatesWrongOrder() throws Exception {
        LogLocationOptimizer.generatePaths(baseDir, "20140103", "20131231");
    }

    @Test
    public void testPathsStringGenerationByDatesEmptyResult() throws Exception {
        assertEquals("", LogLocationOptimizer.generatePaths(baseDir, "20131231", "20140103"));
    }

    private void generateDirs(String fromDate, String toDate) throws Exception {
        DateFormat dateFormat = new SimpleDateFormat(MetricParameter.PARAM_DATE_FORMAT);

        Calendar from = Calendar.getInstance();
        from.setTime(dateFormat.parse(fromDate));

        Calendar to = Calendar.getInstance();
        to.setTime(dateFormat.parse(toDate));

        while (!from.after(to)) {
            StringBuilder sb = new StringBuilder();
            sb.append(baseDir).append(File.separator);
            sb.append(from.get(Calendar.YEAR)).append(File.separator);

            int month = from.get(Calendar.MONTH) + 1;
            sb.append(month < 10 ? "0" : "").append(month).append(File.separator);

            int day = from.get(Calendar.DAY_OF_MONTH);
            sb.append(day < 10 ? "0" : "").append(day);

            new File(sb.toString()).mkdirs();

            from.add(Calendar.DAY_OF_MONTH, 1);
        }
    }
}	
