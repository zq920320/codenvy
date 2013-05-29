/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server;

import com.codenvy.analytics.shared.TimeLineViewData;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;


/** The server side implementation of the RPC service. */
@SuppressWarnings("serial")
public abstract class PersisteableService extends RemoteServiceServlet {

    protected void saveTablesToFile(Object value, File file) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

        try {
            out.writeObject(value);
        } finally {
            out.close();
        }
    }

    @SuppressWarnings("unchecked")
    protected List<TimeLineViewData> loadTablesFromFile(File file) throws IOException {
        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));

        try {
            return (List<TimeLineViewData>)in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } finally {
            in.close();
        }
    }
}
