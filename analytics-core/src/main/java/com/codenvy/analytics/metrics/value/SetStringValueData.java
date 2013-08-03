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

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class SetStringValueData extends SetValueData<String> {

    public static final SetStringValueData DEFAULT = new SetStringValueData(new ArrayList<String>(0));

    public SetStringValueData(ObjectInputStream in) throws IOException {
        super(readFrom(in));
    }

    public SetStringValueData(Collection<String> value) {
        super(value);
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData createInstance(Set<String> value) {
        return new SetStringValueData(value);
    }

    /** {@inheritDoc} */
    @Override
    protected void writeItem(ObjectOutputStream out, String item) throws IOException {
        out.writeUTF(item);
    }

    private static Collection<String> readFrom(ObjectInputStream in) throws IOException {
        int size = in.readInt();
        Set<String> result = new HashSet<>(size);

        for (int i = 0; i < size; i++) {
            result.add(in.readUTF());
        }

        return result;
    }
}
