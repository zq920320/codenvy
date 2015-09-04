/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.gae.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Ann Shumilova
 */
@DTO
public interface ApplicationInfo {
    String getWebURL();

    void setWebURL(String webURL);

    String getApplicationId();

    void setApplicationId(String applicationId);

    ApplicationInfo withWebURL(String webURL);

    ApplicationInfo withApplicationId(String applicationId);
}