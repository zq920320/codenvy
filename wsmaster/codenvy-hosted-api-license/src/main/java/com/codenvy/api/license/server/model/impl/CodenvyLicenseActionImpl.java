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
package com.codenvy.api.license.server.model.impl;

import com.codenvy.api.license.model.CodenvyLicenseAction;
import com.codenvy.api.license.model.Constants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Map;
import java.util.Objects;

/**
 * Data object of {@link CodenvyLicenseAction}.
 *
 * @author Anatolii Bazko
 */
@Entity(name = "lic")
@NamedQueries({})
@Table(indexes = {})
public class CodenvyLicenseActionImpl implements CodenvyLicenseAction {
    @Column(nullable = false)
    private final Constants.TYPE licenseType;

    @Transient
    private final String licenseId;

    @Column(nullable = false)
    private final Constants.Action actionType;

    @Column(nullable = false)
    private final long actionTimestamp;

    @Transient
    private final Map<String, String> attributes;

    public CodenvyLicenseActionImpl(Constants.TYPE licenseType,
                                    Constants.Action actionType,
                                    long actionTimestamp,
                                    String licenseId,
                                    Map<String, String> attributes) {
        this.licenseType = licenseType;
        this.licenseId = licenseId;
        this.actionType = actionType;
        this.actionTimestamp = actionTimestamp;
        this.attributes = attributes;
    }

    @Override
    public Constants.TYPE getLicenseType() {
        return licenseType;
    }

    @Override
    public String getLicenseId() {
        return licenseId;
    }

    @Override
    public Constants.Action getActionType() {
        return actionType;
    }

    @Override
    public long getActionTimestamp() {
        return actionTimestamp;
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CodenvyLicenseActionImpl)) {
            return false;
        }
        final CodenvyLicenseActionImpl that = (CodenvyLicenseActionImpl)obj;
        return Objects.equals(licenseType, that.licenseType)
               && Objects.equals(licenseId, that.licenseId)
               && Objects.equals(actionType, that.actionType)
               && Objects.equals(actionTimestamp, that.actionTimestamp)
               && getAttributes().equals(that.getAttributes());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(licenseType);
        hash = 31 * hash + Objects.hashCode(licenseId);
        hash = 31 * hash + Objects.hashCode(actionType);
        hash = 31 * hash + Objects.hashCode(actionTimestamp);
        hash = 31 * hash + getAttributes().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "CodenvyLicenseActionImpl{" +
               "licenseType=" + licenseType +
               ", licenseId='" + licenseId + '\'' +
               ", actionType=" + actionType +
               ", actionTimestamp=" + actionTimestamp +
               ", attributes=" + attributes +
               '}';
    }
}
