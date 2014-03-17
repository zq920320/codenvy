/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.services.reports;

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;

import javax.xml.bind.annotation.XmlElement;
import java.text.ParseException;
import java.util.Calendar;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractFrequencyConfiguration {

    private ViewsConfiguration           views;
    private ContextModifierConfiguration contextModifier;

    @XmlElement(name = "views")
    public void setViews(ViewsConfiguration views) {
        this.views = views;
    }

    public ViewsConfiguration getViews() {
        return views;
    }

    @XmlElement(name = "context-modifier")
    public void setContextModifier(ContextModifierConfiguration contextModifier) {
        this.contextModifier = contextModifier;
    }

    public ContextModifierConfiguration getContextModifier() {
        return contextModifier;
    }

    public Context initContext(Context context) throws ParseException {
        Calendar toDate = context.getAsDate(Parameters.TO_DATE);
        toDate.add(Calendar.DAY_OF_MONTH, -1);

        Context.Builder builder = new Context.Builder(context);
        builder.put(getTimeUnit());

        return Utils.initDateInterval(toDate, builder);
    }

    abstract public boolean isAppropriateDateToSendReport(Context context) throws ParseException;

    abstract public Parameters.TimeUnit getTimeUnit();
}
