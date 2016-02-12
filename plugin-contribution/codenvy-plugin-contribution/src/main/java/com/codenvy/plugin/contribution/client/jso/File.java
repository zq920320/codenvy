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
