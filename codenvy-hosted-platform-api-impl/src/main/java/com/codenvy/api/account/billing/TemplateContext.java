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
package com.codenvy.api.account.billing;

import org.thymeleaf.context.AbstractContext;
import org.thymeleaf.context.ContextExecutionInfo;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IContextExecutionInfo;

import java.util.Calendar;

/**
 * Simple thymeleaf template context implementation.
 * @author Max Shaposhnik
 *
 */
public class TemplateContext extends AbstractContext implements IContext {

    @Override
    protected IContextExecutionInfo buildContextExecutionInfo(String templateName) {
        final Calendar now = Calendar.getInstance();
        return new ContextExecutionInfo(templateName, now);
    }
}
