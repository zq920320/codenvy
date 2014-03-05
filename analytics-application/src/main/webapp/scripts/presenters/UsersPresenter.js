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
if (typeof analytics === "undefined") {
    analytics = {};
}

analytics.presenter = analytics.presenter || {};

analytics.presenter.UsersPresenter = function UsersPresenter() {};

analytics.presenter.UsersPresenter.prototype = new EntryViewPresenter();

analytics.presenter.UsersPresenter.prototype.TARGET_PAGE_LINK = "users-view.jsp";

analytics.presenter.UsersPresenter.prototype.mapColumnNameToSortValue = {
        "Email": "user",
//        "First Name": "user_first_name",
//        "Last Name": "user_last_name",
//        "Company": "user_company",            
//        "Job": "user_job",
        "# Sessions": "sessions",
        "# Usage Time": "time",
        "# Projects": "projects",
};