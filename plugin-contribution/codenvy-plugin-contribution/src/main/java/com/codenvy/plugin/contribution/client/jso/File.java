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

import java.util.Date;

/**
 * Interface for FileAPI File type.
 */
public interface File {

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

    /**
     * Retuns the last modified date of the file. On getting, if user agents can make this information available,<br>
     * this must return a new Date[HTML] object initialized to the last modified date of the file. If the last<br>
     * modification date and time are not known, the attribute must return the current date and time as a Date object.
     *
     * @return the last modification date
     */
    Date getLastModifiedDate();

    /**
     * Returns the name of the file; on getting, this must return the name of the file as a string. There are<br>
     * numerous file name variations on different systems; this is merely the name of the file, without path<br>
     * information.<br>
     * On getting, if user agents cannot make this information available, they must return the empty string.
     *
     * @return the name of the file
     */
    String getName();
}
