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
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ListListStringValueData extends ListValueData<ListStringValueData> {

    private static final long serialVersionUID = 1L;

    public static final ListListStringValueData EMPTY =
            new ListListStringValueData(Collections.<ListStringValueData>emptyList());

    public ListListStringValueData() {
        super();
    }

    public ListListStringValueData(Collection<ListStringValueData> value) {
        super(value);
    }

    @Override
    protected ValueData createInstance(List<ListStringValueData> value) {
        return new ListListStringValueData(value);
    }

    /** {@inheritedDoc} */
    @Override
    protected void writeItem(ObjectOutput out, ListStringValueData item) throws IOException {
        out.writeObject(item);
    }

    /** {@inheritedDoc} */
    @Override
    protected ListStringValueData readItem(ObjectInput in) throws IOException {
        try {
            return (ListStringValueData)in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
}
