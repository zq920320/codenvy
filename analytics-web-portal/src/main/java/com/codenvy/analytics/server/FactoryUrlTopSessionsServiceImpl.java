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

import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.executor.ScriptExecutor;
import com.codenvy.analytics.server.vew.template.Display;
import com.codenvy.analytics.shared.TableData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopSessionsServiceImpl extends AbstractService {

    private static final Logger  LOGGER           = LoggerFactory.getLogger(FactoryUrlTopSessionsServiceImpl.class);
    private static final Display DISPLAY          = Display.initialize("view/factory-url-top-sessions.xml");
    private static final String  FILE_NAME_PREFIX = "factory-url-top-sessions";

    public List<TableData> getData(Map<String, String> filter) {
        try {
            Map<String, String> context = Utils.newContext();
            MetricParameter.FROM_DATE.putDefaultValue(context);
            MetricParameter.TO_DATE.putDefaultValue(context);

            return super.getData(context, filter);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<String> getFactoryUrls(String field, String param) throws IOException {
        Map<String, String> context = Utils.newContext();

        MetricParameter.LOAD_DIR.put(context, Utils.getLoadDirFor(MetricType.FACTORY_CREATED));
        MetricParameter.FIELD.put(context, field);
        MetricParameter.PARAM.put(context, param);

        ListStringValueData valueData =
                (ListStringValueData)ScriptExecutor.INSTANCE
                                                   .executeAndReturn(ScriptType.FACTORY_URL_BY_ENTITY, context);

        return valueData.getAll();
    }

    /** {@inheritDoc} */
    @Override
    protected String getFileName(Map<String, String> context) {
        return FILE_NAME_PREFIX;
    }

    /** {@inheritDoc} */
    @Override
    protected Display[] getDisplays() {
        return new Display[]{DISPLAY};
    }
}
