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


package com.codenvy.analytics.server;

import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.server.vew.template.Display;
import com.codenvy.analytics.shared.TableData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/** The server side implementation of the RPC service. */
@SuppressWarnings("serial")
public class FactoryUrlTimeLineServiceImpl extends AbstractService {

    private static final Logger  LOGGER           = LoggerFactory.getLogger(FactoryUrlTimeLineServiceImpl.class);
    private static final Display DISPLAY          = Display.initialize("view/factory-url-time-line.xml");
    private static final String  FILE_NAME_PREFIX = "factory-url-timeline";

    public List<TableData> getData(TimeUnit timeUnit, Map<String, String> filterContext) {
        try {
            Map<String, String> context = Utils.initializeContext(timeUnit);
            context.putAll(filterContext);

            return super.getData(DISPLAY, context);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void update() {
        try {
            Map<String, String> context = Utils.initializeContext(TimeUnit.DAY);
            calculateAndSave(DISPLAY, context);

            context = Utils.initializeContext(TimeUnit.WEEK);
            calculateAndSave(DISPLAY, context);

            context = Utils.initializeContext(TimeUnit.MONTH);
            calculateAndSave(DISPLAY, context);

            context = Utils.initializeContext(TimeUnit.LIFETIME);
            calculateAndSave(DISPLAY, context);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected String getBinFileName(Map<String, String> context) {
        TimeUnit timeUnit = Utils.getTimeUnit(context);
        return FILE_NAME_PREFIX + "_" + timeUnit.name() + ".bin";

    }

    /** {@inheritDoc} */
    @Override
    protected String getCsvFileName(Map<String, String> context) {
        TimeUnit timeUnit = Utils.getTimeUnit(context);
        return FILE_NAME_PREFIX + "_" + timeUnit.name() + ".csv";
    }
}
