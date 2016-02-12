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
import com.google.gwt.core.client.JsArray;

/**
 * Javascript Overlay class over Blob objects.
 */
public class JsBlob extends JavaScriptObject implements Blob {

    /** JSO mandated protected constructor. */
    protected JsBlob() {
    }

    /**
     * Creates a new Blob object.
     *
     * @return a Blob
     */
    public static final native JsBlob create() /*-{
        return new Blob();
    }-*/;

    /**
     * Creates a new Blob object.
     *
     * @param blobParts
     *         parts appended to the blob
     * @return a Blob
     */
    public static final native JsBlob create(JsArray<?> blobParts) /*-{
        return new Blob(blobParts);
    }-*/;

    /**
     * Creates a new Blob object.
     *
     * @param blobParts
     *         parts appended to the blob
     * @param mediaType
     *         the media type of the resulting blob
     * @return a Blob
     */
    public static final native JsBlob create(JsArray<?> blobParts, String mediaType) /*-{
        return new Blob(blobParts, {"type": mediaType});
    }-*/;

    /**
     * Creates a new Blob object.
     *
     * @param blobParts
     *         parts appended to the blob
     * @return a Blob
     */
    public static final native JsBlob create(String blobPart) /*-{
        return new Blob([blobPart]);
    }-*/;

    /**
     * Creates a new Blob object.
     *
     * @param blobParts
     *         parts appended to the blob
     * @return a Blob
     */
    public static final native JsBlob create(String blobPart, String mediaType) /*-{
        return new Blob([blobPart], {"type": mediaType});
    }-*/;

    @Override
    public final native double getSize() /*-{
        return this.size;
    }-*/;

    @Override
    public final native String getType() /*-{
        return this.type;
    }-*/;
}
