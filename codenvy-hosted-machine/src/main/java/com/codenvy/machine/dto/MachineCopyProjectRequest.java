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
package com.codenvy.machine.dto;

import org.eclipse.che.api.machine.shared.ProjectBinding;
import org.eclipse.che.dto.shared.DTO;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface MachineCopyProjectRequest {
    String getWorkspaceId();

    void setWorkspaceId(String workspaceId);

    MachineCopyProjectRequest withWorkspaceId(String workspaceId);

    ProjectBinding getProject();

    void setProject(ProjectBinding projectBinding);

    MachineCopyProjectRequest withProject(ProjectBinding projectBinding);

    String getHostFolder();

    void setHostFolder(String hostFolder);

    MachineCopyProjectRequest withHostFolder(String hostFolder);

    String getToken();

    void setToken(String token);

    MachineCopyProjectRequest withToken(String token);
}
