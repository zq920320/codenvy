/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
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
package ut.com.codenvy.plugin;

import org.junit.Test;

import com.codenvy.plugin.CodenvyPluginComponent;
import com.codenvy.plugin.CodenvyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class CodenvyPluginComponentUnitTest {
    @Test
    public void testGetName() {
        CodenvyPluginComponent component = new CodenvyPluginComponentImpl(null);
        assertEquals("names do not match!", "codenvyPluginComponent", component.getName());
    }
}
