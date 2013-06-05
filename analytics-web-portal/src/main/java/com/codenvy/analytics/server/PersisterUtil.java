/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server;

import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.shared.TableData;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PersisterUtil {

    public static void saveTablesToFile(Object value, String fileName) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(getFile(fileName))));

        try {
            out.writeObject(value);
        } finally {
            out.close();
        }
    }

    @SuppressWarnings("unchecked")
    public static List<TableData> loadTablesFromFile(String fileName) throws IOException {
        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(getFile(fileName))));

        try {
            return (List<TableData>)in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } finally {
            in.close();
        }
    }

    private static File getFile(String fileName) {
        return new File(FSValueDataManager.RESULT_DIRECTORY, fileName);
    }
}
