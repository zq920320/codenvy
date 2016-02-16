/*
 *  [2012] - [2016] Codenvy
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
package it.com.codenvy.plugin;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import com.codenvy.plugin.CodenvyPluginComponent;

import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
public class CodenvyComponentWiredTest {
    private final ApplicationProperties  applicationProperties;
    private final CodenvyPluginComponent codenvyPluginComponent;

    public CodenvyComponentWiredTest(ApplicationProperties applicationProperties, CodenvyPluginComponent codenvyPluginComponent) {
        this.applicationProperties = applicationProperties;
        this.codenvyPluginComponent = codenvyPluginComponent;
    }

    @Test
    public void testMyName() {
        assertEquals("names do not match!", "codenvyPluginComponent:" + applicationProperties.getDisplayName(), codenvyPluginComponent.getName());
    }
}
