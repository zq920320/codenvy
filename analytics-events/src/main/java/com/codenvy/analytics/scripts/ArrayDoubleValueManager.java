/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.scripts;

import org.apache.pig.data.Tuple;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ArrayDoubleValueManager implements ValueManager {

    @Override
    public Double[] valueOf(Tuple tuple) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double[] load(BufferedReader reader) throws IOException {
        List<String> strList = new ArrayList<String>();

        String line;
        while ((line = reader.readLine()) != null) {
            strList.add(line);
        }

        return valueOf(strList);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void store(BufferedWriter writer, Object value) throws IOException {
        if (value instanceof Double[]) {
            Double[] numbers = (Double[])value;

            for (Double number : numbers) {
                writer.write(number.toString());
                writer.newLine();
            }

            writer.flush();
        } else {
            throw new IOException("Unknown class " + value.getClass().getName() + " for storing");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double[] valueOf(String value) throws IOException {
        int beginIndex = value.startsWith("[") ? 1 : 0;
        int endIndex = value.endsWith("]") ? value.length() - 1 : value.length();
        value = value.substring(beginIndex, endIndex);

        List<String> strList = Arrays.asList(value.split(","));
        return valueOf(strList);
    }
    
    private Double[] valueOf(List<String> strList) {
        Double[] result = new Double[strList.size()];
        for (int i = 0; i < strList.size(); i++) {
            result[i] = Double.valueOf(strList.get(i));
        }

        return result;
    }
}
