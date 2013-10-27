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
public class FixedListLongValueData extends FixedListValueData<Long> {

    public static final FixedListLongValueData DEFAULT = new FixedListLongValueData(new ArrayList<Long>(0));

    public FixedListLongValueData(ObjectInputStream in) throws IOException {
        super(readFrom(in));
    }

    public FixedListLongValueData(List<Long> value) {
        super(value);
    }

    /** {@inheritDoc} */
    @Override
    protected Long unionItems(Long v1, Long v2) {
        return v1 + v2;
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData createInstance(List<Long> value) {
        return new FixedListLongValueData(value);
    }

    /** {@inheritDoc} */
    @Override
    protected void writeItem(ObjectOutputStream out, Long item) throws IOException {
        out.writeLong(item);
    }

    /** Deserialization */
    private static Collection<Long> readFrom(ObjectInputStream in) throws IOException {
        int count = in.readInt();

        List<Long> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(in.readLong());
        }

        return list;
    }

}
