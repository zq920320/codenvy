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

import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.old_metrics.value.ValueData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class CassandraDataManager {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraDataManager.class);

    /** {@inheritDoc} */
    public static ValueData loadValue(String columnFamily, Map<String, String> clauses) throws IOException {
        validateDateParams(clauses);

        return null;
        // TODO
    }

    public static void test() {
    }

    /** Returns the file to store in or load value from. */
//    protected static File getFile(MetricType metricType, String fileName, LinkedHashMap<String, String> uuid)
//            throws IOException {
//        File dir = new File(RESULT_DIRECTORY);
//
//        validateDateParams(uuid);
//
//        StringBuilder builder = new StringBuilder();
//        builder.append(metricType.toString().toLowerCase());
//        builder.append(File.separatorChar);
//
//        for (Entry<String, String> entry : uuid.entrySet()) {
//            String element = entry.getValue().toLowerCase();
//
//            if (Parameters.TO_DATE.isParam(entry.getKey())) {
//                element = translateDateToRelativePath(entry.getValue());
//            } else {
//                for (MetricFilter metricFilter : MetricFilter.values()) {
//                    if (metricFilter.name().equals(entry.getKey())) {
//                        switch (metricFilter) {
//                            case REFERRER_URL:
//                            case REPOSITORY_URL:
//                            case FACTORY_URL:
//                                element = getRelativePath("" + entry.getValue().hashCode());
//                                break;
//                            default:
//                                element = getRelativePath(entry.getValue());
//                        }
//                        break;
//                    }
//                }
//            }
//
//            builder.append(element);
//            builder.append(File.separatorChar);
//        }
//
//        builder.append(fileName);
//        return new File(dir, builder.toString());
//    }


    /**
     * Makes sure that {@link com.codenvy.analytics.metrics.Parameters#TO_DATE} and {@link
     * com.codenvy.analytics.metrics.Parameters#FROM_DATE} are the same, otherwise {@link IllegalStateException}
     * will be thrown.
     */
    private static void validateDateParams(Map<String, String> clauses) throws IllegalStateException {
        if (!Parameters.TO_DATE.exists(clauses) || !Parameters.FROM_DATE.exists(clauses) ||
            !Parameters.TO_DATE.get(clauses).equals(Parameters.FROM_DATE.get(clauses))) {

            throw new IllegalStateException("The date params are different or absent in context");
        }
    }


    /**
     * Translate date from format yyyyMMdd into format like yyyy/MM/dd and {@link java.io.File#separatorChar} is used
     * as
     * delimiter.
     */
    private static String translateDateToRelativePath(String date) {
        StringBuilder builder = new StringBuilder();

        builder.append(date.substring(0, 4));
        builder.append(File.separatorChar);
        builder.append(date.substring(4, 6));
        builder.append(File.separatorChar);
        builder.append(date.substring(6, 8));

        return builder.toString();
    }
}
