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
import com.google.gwt.dom.client.FormElement;

/**
 * Javascript Overlay class over FormData objects.
 */
public class FormData extends JavaScriptObject {

    /** JSO mandated protected constructor. */
    protected FormData() {
    }

    /**
     * Creates a new FormData.
     *
     * @return a FormData
     */
    public static final native FormData create() /*-{
        return new FormData();
    }-*/;

    /**
     * Creates a new FormData linked to the form element
     *
     * @return a FormData
     */
    public static final native FormData create(FormElement form) /*-{
        return new FormData(form);
    }-*/;

    /**
     * Creates a new FormData linked to the form element
     *
     * @return a FormData
     */
    public static final native FormData create(elemental.html.FormElement form) /*-{
        return new FormData(form);
    }-*/;

    /**
     * Appends a key/value pair to the form data.
     *
     * @param name
     *         the key
     * @param value
     *         the value
     */
    public final native void append(String name, String value) /*-{
        this.append(name, value);
    }-*/;

    /**
     * Appends a key/value pair to the form data.
     *
     * @param name
     *         the key
     * @param value
     *         the value
     */
    public final native void append(String name, int value) /*-{
        this.append(name, value);
    }-*/;

    /**
     * Appends a key/value pair to the form data.
     *
     * @param name
     *         the key
     * @param value
     *         the value
     */
    public final native void append(String name, double value) /*-{
        this.append(name, value);
    }-*/;

    /**
     * Appends a key/value pair to the form data.
     *
     * @param name
     *         the key
     * @param value
     *         the value
     */
    public final native void append(String name, Blob value) /*-{
        this.append(name, value);
    }-*/;

    /**
     * Appends a key/value pair to the form data.
     *
     * @param name
     *         the key
     * @param value
     *         the value
     * @param filename
     *         the file name reported to the server
     */
    public final native void append(String name, Blob value, String filename) /*-{
        this.append(name, value, filename);
    }-*/;

    /**
     * Appends a key/value pair to the form data.
     *
     * @param name
     *         the key
     * @param value
     *         the value
     */
    public final native void append(String name, File value) /*-{
        this.append(name, value);
    }-*/;

    /**
     * Appends a key/value pair to the form data.
     *
     * @param name
     *         the key
     * @param value
     *         the value
     * @param filename
     *         the file name reported to the server
     */
    public final native void append(String name, File value, String filename) /*-{
        this.append(name, value, filename);
    }-*/;
}
