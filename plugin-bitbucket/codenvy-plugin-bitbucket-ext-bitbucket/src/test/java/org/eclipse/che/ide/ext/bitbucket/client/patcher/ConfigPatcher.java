/*
 *  [2012] - [2016] Codenvy, S.A.
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
package org.eclipse.che.ide.ext.bitbucket.client.patcher;

import com.googlecode.gwt.test.patchers.PatchClass;
import com.googlecode.gwt.test.patchers.PatchMethod;

import org.eclipse.che.ide.util.Config;


/**
 * Patcher for Utils class. Replace native method into Utils.
 *
 * @author Kevin Pollet
 */
@PatchClass(Config.class)
public class ConfigPatcher {
    public static final String WORKSPACE_NAME = "workspaceName";

    /** Patch getWorkspaceName method. */
    @PatchMethod(override = true)
    public static String getWorkspaceName() {
        return WORKSPACE_NAME;
    }
}