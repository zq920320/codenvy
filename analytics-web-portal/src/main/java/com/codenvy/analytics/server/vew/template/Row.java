/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.template;


import com.codenvy.analytics.shared.RowData;

import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public interface Row {

    /**
     * Fills row.
     *
     * @param context the execution context
     * @param columnsCount how many columns should be filled
     * @param overrideContextRule
     */
    List<RowData> retrieveData(Map<String, String> context, int columnsCount,
                               Table.TimeIntervalRule overrideContextRule) throws Exception;
}