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

import org.testng.annotations.Test;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestLogLocationOptimizer extends BasePigTest {
    @Test
    public void testPathsListGeneration() throws Exception {
        final String baseDir = "BaseDir";
        List<String> correctPaths = new LinkedList<String>();
        correctPaths.add(baseDir + File.separator + "2013" + File.separator + "12" + File.separator + "31");
        correctPaths.add(baseDir + File.separator + "2014" + File.separator + "01" + File.separator + "01");
        correctPaths.add(baseDir + File.separator + "2014" + File.separator + "01" + File.separator + "02");
        correctPaths.add(baseDir + File.separator + "2014" + File.separator + "01" + File.separator + "03");

        assertEquals(correctPaths, LogLocationOptimizer.generatePathList(baseDir, "20131231", "20140103"));
    }

    @Test
    public void testPathsStringGeneration() throws Exception {
        final String baseDir = "BaseDir";
        StringBuilder sb = new StringBuilder();
        sb.append(baseDir + File.separator + "2013" + File.separator + "12" + File.separator + "31, ");
        sb.append(baseDir + File.separator + "2014" + File.separator + "01" + File.separator + "01, ");
        sb.append(baseDir + File.separator + "2014" + File.separator + "01" + File.separator + "02, ");
        sb.append(baseDir + File.separator + "2014" + File.separator + "01" + File.separator + "03");


        assertEquals(sb.toString(), LogLocationOptimizer.generatePathString(baseDir, "20131231", "20140103"));
    }
}	
