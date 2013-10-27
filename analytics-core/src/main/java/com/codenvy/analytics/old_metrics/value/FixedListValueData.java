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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * The list is supposed to be with fixed size. So the union of corresponding items
 * is used rather then addition items.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class FixedListValueData<T> extends ListValueData<T> {

    public FixedListValueData(Collection<T> value) {
        super(value);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected ValueData doUnion(ValueData valueData) {
        List<T> value1 = getAll();
        List<T> value2 = ((FixedListValueData<T>)valueData).getAll();

        if (value1.size() != value2.size()) {
            throw new IllegalStateException("The sizes of the lists are different");
        }

        List<T> newValue = new ArrayList<>(value1.size());
        for (int i = 0; i < value1.size(); i++) {
            newValue.add(unionItems(value1.get(i), value2.get(i)));
        }

        return createInstance(newValue);
    }

    protected abstract T unionItems(T v1, T v2);
}
