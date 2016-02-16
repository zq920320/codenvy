/*
 *  [2012] - [2016] Codenvy
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
// Javascript used on the Codenvy admin page
AJS.toInit(function() {
    var baseUrl = AJS.contextPath();

    function populateForm() {
        AJS.$.ajax({
            url: baseUrl + "/rest/codenvy-admin/1.0/",
            dataType: "json",
            success: function(config) {
                AJS.$("#instanceUrl").attr("value", config.instanceUrl);
                AJS.$("#username").attr("value", config.username);
                AJS.$("#password").attr("value", config.password);
            }
        });
    }
    function updateConfig() {
        AJS.$.ajax({
            url: baseUrl + "/rest/codenvy-admin/1.0/",
            type: "PUT",
            contentType: "application/json",
            data: '{ "instanceUrl": "' + AJS.$("#instanceUrl").attr("value") + '", "username": "' + AJS.$("#username").attr("value") + '", "password": "' + AJS.$("#password").attr("value") + '" }',
            processData: false
        }).done(function( data ) {
            alert("Codenvy data successfully saved.");
        });
    }
    populateForm();

    // Submit new Codenvy admin data
    AJS.$("#admin").submit(function(e) {
        e.preventDefault();
        updateConfig();
    });
});
