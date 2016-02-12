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
package org.eclipse.che.ide.ext.bitbucket.shared;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Represents a Bitbucket repository.
 *
 * @author Kevin Pollet
 */
@DTO
public interface BitbucketRepository {
    String getName();

    void setName(String name);

    BitbucketRepository getParent();

    void setParent(BitbucketRepository parent);

    boolean isIsPrivate();

    void setIsPrivate(boolean isIsPrivate);

    BitbucketRepositoryLinks getLinks();

    void setLinks(BitbucketRepositoryLinks links);

    BitbucketUser getOwner();

    void setOwner(BitbucketUser owner);

    @DTO
    interface BitbucketRepositoryLinks {
        List<BitbucketLink> getClone();

        void setClone(List<BitbucketLink> clone);
    }
}
