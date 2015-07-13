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

/**
 * Interface for FileAPI Blob type.
 */
public interface Blob {

    /**
     * Returns the size of the Blob object in bytes. On getting, conforming user agents must return<br>
     * the total number of bytes that can be read by a FileReader or FileReaderSync object, or 0 if<br>
     * the Blob has no bytes to be read. If the Blob has been neutered with close called on it, then<br>
     * size must return 0.
     *
     * @return the size
     */
    double getSize();

    /**
     * Returns The ASCII-encoded string in lower case representing the media type of the Blob. For File<br>
     * objects that are returned from the underlying file system, user agents must return the type of a<br>
     * Blob as an ASCII-encoded string in lower case, such that when it is converted to a corresponding<br>
     * byte sequence, it is a parsable MIME type [MIMESNIFF], or the empty string -- 0 bytes -- if the<br>
     * type cannot be determined. When the Blob is of type text/plain user agents must NOT append a charset<br>
     * parameter to the dictionary of parameters portion of the media type [MIMESNIFF]. User agents must not<br>
     * attempt heuristic determination of encoding, including statistical methods.
     *
     * @return the media type
     */
    String getType();
}
