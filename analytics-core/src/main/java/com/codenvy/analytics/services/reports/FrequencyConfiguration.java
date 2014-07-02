/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.services.reports;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@XmlRootElement(name = "frequency")
public class FrequencyConfiguration {
    private DailyFrequencyConfiguration   daily;
    private WeeklyFrequencyConfiguration  weekly;
    private MonthlyFrequencyConfiguration monthly;

    @XmlElement(name = "daily")
    public void setDaily(DailyFrequencyConfiguration daily) {
        this.daily = daily;
    }

    public DailyFrequencyConfiguration getDaily() {
        return daily;
    }

    @XmlElement(name = "weekly")
    public void setWeekly(WeeklyFrequencyConfiguration weekly) {
        this.weekly = weekly;
    }

    public WeeklyFrequencyConfiguration getWeekly() {
        return weekly;
    }

    @XmlElement(name = "monthly")
    public void setMonthly(MonthlyFrequencyConfiguration monthly) {
        this.monthly = monthly;
    }

    public MonthlyFrequencyConfiguration getMonthly() {
        return monthly;
    }

    public List<AbstractFrequencyConfiguration> frequencies() {
        List<AbstractFrequencyConfiguration> result = new ArrayList<>();
        if (daily != null) {
            result.add(daily);
        }

        if (weekly != null) {
            result.add(weekly);
        }

        if (monthly != null) {
            result.add(monthly);
        }

        return result;
    }
}
