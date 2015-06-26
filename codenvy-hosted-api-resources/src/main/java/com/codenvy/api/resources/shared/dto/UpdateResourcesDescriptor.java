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
package com.codenvy.api.resources.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Sergii Leschenko
 */
@DTO
public interface UpdateResourcesDescriptor {
    void setWorkspaceId(String workspaceId);

    String getWorkspaceId();

    UpdateResourcesDescriptor withWorkspaceId(String workspaceId);

    void setRunnerRam(Integer runnerRam);

    Integer getRunnerRam();

    UpdateResourcesDescriptor withRunnerRam(Integer runnerRam);

    void setRunnerTimeout(Integer runnerTimeout);

    Integer getRunnerTimeout();

    UpdateResourcesDescriptor withRunnerTimeout(Integer runnerTimeout);

    void setBuilderTimeout(Integer builderTimeout);

    Integer getBuilderTimeout();

    UpdateResourcesDescriptor withBuilderTimeout(Integer builderTimeout);

    void setResourcesUsageLimit(Double gbHUsageLimit);

    Double getResourcesUsageLimit();

    UpdateResourcesDescriptor withResourcesUsageLimit(Double gbHUsageLimit);
}
