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


package com.codenvy.analytics.server.vew.template;


import com.codenvy.analytics.shared.RowData;

import org.w3c.dom.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
class TitleRow extends AbstractRow {

    private static final String ATTRIBUTE_NAME = "name";

    private final String[] titles;

    /** {@link TitleRow} constructor. */
    private TitleRow(String[] titles) {
        this.titles = titles;
    }

    /** {@inheritDoc} */
    @Override
    protected String doRetrieve(Map<String, String> context, int columnNumber) throws IOException {
        if (columnNumber < titles.length) {
            return titles[columnNumber];
        } else {
            return "";
        }
    }

    /** Factory method */
    public static TitleRow initialize(Element element) {
        String names = element.getAttribute(ATTRIBUTE_NAME);
        return new TitleRow(names.split(","));
    }

    /** {@inheritDoc} */
    @Override
    protected Map<String, String> prevDateInterval(Map<String, String> context,
                                                   Table.TimeIntervalRule overrideContextRule) throws IOException {
        return context;
    }
}