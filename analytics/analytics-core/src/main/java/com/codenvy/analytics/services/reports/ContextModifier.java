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
package com.codenvy.analytics.services.reports;

import com.codenvy.analytics.metrics.Context;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public interface ContextModifier {

    /**
     * Updates execution context.
     *
     * @param context
     *         the execution context, contains {@link com.codenvy.analytics.metrics.Parameters#RECIPIENT} as
     * @return
     */
    Context update(Context context);
}
