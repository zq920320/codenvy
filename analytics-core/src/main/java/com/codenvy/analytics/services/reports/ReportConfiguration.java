/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.services.reports;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Anatoliy Bazko
 */
@XmlRootElement(name = "report")
public class ReportConfiguration {
    private RecipientsConfiguration      recipients;
    private List<FrequencyConfiguration> frequencies;

    @XmlElement(name = "recipients")
    public void setRecipients(RecipientsConfiguration recipients) {
        this.recipients = recipients;
    }

    public RecipientsConfiguration getRecipients() {
        return recipients;
    }

    @XmlElement(name = "frequency")
    public void setFrequencies(List<FrequencyConfiguration> frequencies) {
        this.frequencies = frequencies;
    }

    public List<FrequencyConfiguration> getFrequencies() {
        return frequencies;
    }
}

