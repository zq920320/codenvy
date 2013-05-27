/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.layout;

import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class AbstractRow implements RowLayout {

    protected String print(String format, ValueData valueData) {
        if (format.contains("d")) {
            return String.format(format, valueData.getAsLong());
        } else if (format.contains("f")) {
            return String.format(format, valueData.getAsDouble());
        } else if (format.contains("s")) {
            return String.format(format, valueData.getAsString());
        }
        
        return valueData.getAsString();
    }

    /** Checks if received value might be displayed in view. */
    protected boolean isPrintable(ValueData value) {
        if (value instanceof DoubleValueData
            && (Double.isNaN(value.getAsDouble()) || Double.isInfinite(value.getAsDouble()) || value.getAsDouble() == 0)) {
            return false;
        } else if (value instanceof LongValueData && value.getAsLong() == 0) {
            return false;
        }
    
        return true;
    }


}
