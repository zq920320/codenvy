/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.pig.udf;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.io.IOException;

/** @author Dmytro Nochevnov */
public class CalculateGigabyteRamHours extends EvalFunc<Double> {

    public static final long GRH_DEVIDER = 3686400000L;  // (number of milliseconds in 1 hour) * (number of MBs in 1 GB)

    /** {@inheritDoc} */
    @Override
    public Double exec(Tuple input) throws IOException {
        if (input == null || input.size() < 2) {
            return null;
        }

        try {
            long memory = (long)input.get(0);
            long usage_time = (long)input.get(1);

            return calculateGigabiteRamHours(memory, usage_time);
        } catch (NumberFormatException | ArithmeticException | NullPointerException e) {
            return null;
        }
    }

    public static double calculateGigabiteRamHours(long memory_mb, long usage_time_msec) {
        return ((double) memory_mb * usage_time_msec) / GRH_DEVIDER;
    }

    /** {@inheritDoc} */
    @Override
    public Schema outputSchema(Schema input) {
        return new Schema(new Schema.FieldSchema(getSchemaName(this.getClass().getName().toLowerCase(), input), DataType.CHARARRAY));
    }
}
