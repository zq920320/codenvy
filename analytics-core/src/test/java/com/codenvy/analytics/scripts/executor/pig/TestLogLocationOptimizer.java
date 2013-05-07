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


import java.io.File;
import java.util.Calendar;
import java.util.UUID;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestLogLocationOptimizer extends BaseTest {

    private String baseDir;

    @BeforeMethod
    public void setUp()
    {
        baseDir = BASE_DIR + File.separator + UUID.randomUUID();
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

    @Test
    public void testPathsStringGenerationByLast10Minutes() throws Exception {
        Calendar cal = Calendar.getInstance();

        File dir1 = new File(generatePath(cal));
        dir1.mkdirs();

        assertEquals(dir1.getPath(), LogLocationOptimizer.generatePaths(baseDir, "10"));
    }

    /**
     * 1440m=1d
     */
    @Test
    public void testPathsStringGenerationByLast1440Minutes() throws Exception {
        Calendar cal = Calendar.getInstance();
        String path2 = generatePath(cal);

        cal.add(Calendar.DAY_OF_MONTH, -1);
        String path1 = generatePath(cal);

        File dir1 = new File(path1);
        File dir2 = new File(path2);

        dir1.mkdirs();
        dir2.mkdirs();

        assertEquals(dir1.getPath() + "," + dir2.getPath(), LogLocationOptimizer.generatePaths(baseDir, "1440"));
    }

    private String generatePath(Calendar cal)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(baseDir).append(File.separator);
        sb.append(cal.get(Calendar.YEAR)).append(File.separator);

        int month = cal.get(Calendar.MONTH) + 1;
        sb.append(month < 10 ? "0" : "").append(month).append(File.separator);

        int day = cal.get(Calendar.DAY_OF_MONTH);
        sb.append(day < 10 ? "0" : "").append(day);

        return sb.toString();
    }
}	
