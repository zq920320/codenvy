/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
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
