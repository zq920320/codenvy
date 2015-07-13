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
package com.codenvy.plugin.contribution.vcs.client.hosting.dto;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface Repository {
    String getName();

    Repository withName(String name);

    String getCloneUrl();

    Repository withCloneUrl(String cloneUrl);

    boolean isFork();

    Repository withFork(boolean isFork);

    boolean isPrivateRepo();

    Repository withPrivateRepo(boolean isPrivateRepo);

    Repository getParent();

    Repository withParent(Repository parent);
}
