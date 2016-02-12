/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.plugin.contribution.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

import java.util.Date;

/**
 * Javascript Overlay class over File objects.
 */
public class JsFile extends JavaScriptObject implements File {

    /** JSO mandated protected constructor. */
    protected JsFile() {
    }

    /**
     * Creates a new file.
     *
     * @return a File
     */
    public static final native JsFile create(Blob blob, String filename) /*-{
        return new File(blob, filename);
    }-*/;

    public final native double getSize() /*-{
        return this.size;
    }-*/;

    public final native String getType() /*-{
        return null;
    }-*/;

    public final native Date getLastModifiedDate() /*-{
        return this.lastModifiedDate;
    }-*/;

    public final native String getName() /*-{
        return this.name;
    }-*/;
}
