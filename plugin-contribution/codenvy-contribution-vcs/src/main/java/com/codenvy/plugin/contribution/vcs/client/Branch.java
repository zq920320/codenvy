/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.plugin.contribution.vcs.client;

import org.eclipse.che.dto.shared.DTO;

/**
 * Representation of a VCS branch.<br>
 * Mirrors the plugin-git interface until we define an abstraction on VCSes and branches.
 */
@DTO
public interface Branch {

    String getName();

    boolean isActive();

    String getDisplayName();

    boolean isRemote();

    void setName(String name);

    void setDisplayName(String displayName);

    void setActive(boolean isActive);

    void setRemote(boolean isRemote);

    Branch withName(String name);

    Branch withDisplayName(String displayName);

    Branch withActive(boolean isActive);

    Branch withRemote(boolean isRemote);
}
