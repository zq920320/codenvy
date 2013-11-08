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
package com.codenvy.analytics.pig.udf;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** MongoDB implementation for {@link org.apache.hadoop.mapreduce.InputSplit} */
public class MongoInputSplit extends InputSplit implements Writable {

    private String serverUrl;

    public MongoInputSplit() {
    }

    public MongoInputSplit(Configuration configuration) {
        serverUrl = configuration.get(MongoLoader.SERVER_URL_PARAM);
    }

    @Override
    public long getLength() throws IOException, InterruptedException {
        return 1;
    }

    @Override
    public String[] getLocations() throws IOException, InterruptedException {
        return new String[]{serverUrl};
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(serverUrl);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        serverUrl = dataInput.readUTF();
    }
}
