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


package com.codenvy.analytics.shared;

import java.util.ArrayList;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class RowData extends ArrayList<String> {

    private static final long serialVersionUID = 1L;
    private Map<String, String> attributes;

    /** Default {@link RowData} constructor for serialization. */
    public RowData() {
    }

    /** {@link RowData} constructor. */
    public RowData(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getCsv() {
        StringBuffer rowBuffer = new StringBuffer(this.size());

        for (String element : this) {
            StringBuffer elementBuffer = new StringBuffer(element.replace("\"", "\"\""));
            elementBuffer.insert(0, '\"');
            elementBuffer.append('\"');
            rowBuffer.append(elementBuffer);
            rowBuffer.append(",");
        }
        rowBuffer.deleteCharAt(rowBuffer.lastIndexOf(","));

        return rowBuffer.toString();
    }
}
