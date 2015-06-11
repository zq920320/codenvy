/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.ide.onpremises.permits;


import com.google.inject.Inject;

import org.eclipse.che.ide.api.action.permits.ResourcesLockedActionPermit;

/**
 * Dummy implementation of resources locked permit for build and run actions.
 *
 * @author Igor Vinokur
 */
public class ResourcesLockedActionPermitImpl implements ResourcesLockedActionPermit {
    @Inject
    public ResourcesLockedActionPermitImpl() {
    }

    @Override
    public boolean isAllowed() {
        return true;
    }

    @Override
    public boolean isAccountLocked() {
        return false;
    }

    @Override
    public boolean isWorkspaceLocked() {
        return false;
    }
}
