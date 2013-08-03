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


package com.codenvy.analytics.metrics.value;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ListStringValueData extends ListValueData<String> {

    public static final ListStringValueData DEFAULT = new ListStringValueData(new ArrayList<String>(0));
    private static final long serialVersionUID = 1L;

    public ListStringValueData() {
    }

    public ListStringValueData(List<String> value) {
        super(value);
    }

    /** {@inheritedDoc} */
    @Override
    protected ValueData createInstance(List<String> value) {
        return new ListStringValueData(value);
    }

    /** {@inheritedDoc} */
    @Override
    protected void writeItem(ObjectOutput out, String item) throws IOException {
        out.writeUTF(item);
    }

    /** {@inheritedDoc} */
    @Override
    protected String readItem(ObjectInput in) throws IOException {
        return in.readUTF();
    }
}
