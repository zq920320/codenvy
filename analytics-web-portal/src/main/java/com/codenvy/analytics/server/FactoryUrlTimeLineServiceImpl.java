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

import java.util.Collections;
import java.util.List;
import java.util.Map;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTimeLineServiceImpl extends AbstractService {

    private static final String    FILE_NAME_PREFIX = "factory-url-timeline";
    private static final Display[] DISPLAYS         =
            new Display[]{Display.initialize("view/factory-url-time-line-1.xml"),
                          Display.initialize("view/factory-url-time-line-2.xml")};

    public List<TableData> getData(TimeUnit timeUnit, Map<String, String> filter) {
        try {
            return super.getData(Utils.initializeContext(timeUnit), filter);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected String getFileName(Map<String, String> context) {
        TimeUnit timeUnit = Utils.getTimeUnit(context);
        return FILE_NAME_PREFIX + "_" + timeUnit.name();
    }

    /** {@inheritDoc} */
    @Override
    protected Display[] getDisplays() {
        return DISPLAYS;
    }
}
