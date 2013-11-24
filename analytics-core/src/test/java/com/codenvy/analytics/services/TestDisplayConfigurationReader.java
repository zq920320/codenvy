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
package com.codenvy.analytics.services;

import com.codenvy.analytics.services.view.DisplayConfiguration;
import com.codenvy.analytics.services.view.RowConfiguration;
import com.codenvy.analytics.services.view.SectionConfiguration;
import com.codenvy.analytics.services.view.ViewConfiguration;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.AssertJUnit.assertEquals;


/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestDisplayConfigurationReader {

    private static final String RESOURCE = "<display>" +
                                           "    <view time-unit=\"day,week\">" +
                                           "        <section name=\"workspaces\" columns=\"20\">" +
                                           "            <description>desc</description>" +
                                           "            <row class=\"Date.class\">" +
                                           "                <parameter key=\"format\" value=\"dd MMM\"/>" +
                                           "            </row>" +
                                           "            <row class=\"Empty.class\" />" +
                                           "        </section>" +
                                           "    </view>" +
                                           "</display>";

    @Test
    public void testParsingConfig() throws Exception {
        XmlConfigurationManager<DisplayConfiguration> spyService =
                spy(new XmlConfigurationManager<>(DisplayConfiguration.class));

        doReturn(new ByteArrayInputStream(RESOURCE.getBytes("UTF-8"))).when(spyService).openResource(anyString());

        DisplayConfiguration displayConfiguration = spyService.loadConfiguration(anyString());
        Assert.assertEquals(1, displayConfiguration.getViews().size());

        ViewConfiguration viewConfiguration = displayConfiguration.getViews().get(0);

        assertEquals("day,week", viewConfiguration.getTimeUnit());
        assertEquals(1, viewConfiguration.getSections().size());

        SectionConfiguration sectionConfiguration = viewConfiguration.getSections().get(0);
        assertEquals(20, sectionConfiguration.getColumns());
        assertEquals("workspaces", sectionConfiguration.getName());
        assertEquals("desc", sectionConfiguration.getDescription());

        assertEquals(2, sectionConfiguration.getRows().size());

        RowConfiguration rowConfiguration = sectionConfiguration.getRows().get(0);
        assertEquals("Date.class", rowConfiguration.getClazz());
        assertEquals(1, rowConfiguration.getParameters().size());
        assertEquals("format", rowConfiguration.getParameters().get(0).getKey());
        assertEquals("dd MMM", rowConfiguration.getParameters().get(0).getValue());

        rowConfiguration = sectionConfiguration.getRows().get(1);
        assertEquals("Empty.class", rowConfiguration.getClazz());
        assertEquals(0, rowConfiguration.getParameters().size());
    }
}
