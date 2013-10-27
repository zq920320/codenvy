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


package com.codenvy.analytics.old_metrics.value;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ListListStringValueData extends ListValueData<ListStringValueData> {

    public static final ListListStringValueData DEFAULT =
            new ListListStringValueData(new ArrayList<ListStringValueData>(0));

    public ListListStringValueData(ObjectInputStream in) throws IOException {
        super(readFrom(in));
    }

    public ListListStringValueData(Collection<ListStringValueData> value) {
        super(value);
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData createInstance(List<ListStringValueData> value) {
        return new ListListStringValueData(value);
    }

    /** {@inheritDoc} */
    @Override
    protected void writeItem(ObjectOutputStream out, ListStringValueData item) throws IOException {
        item.writeTo(out);
    }

    /** Deserialization */
    private static Collection<ListStringValueData> readFrom(ObjectInputStream in) throws IOException {
        int count = in.readInt();

        List<ListStringValueData> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(new ListStringValueData(in));
        }

        return list;
    }
}
