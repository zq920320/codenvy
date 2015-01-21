/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
function get_section_body(section) {
 for (var i = 0; i < section.childNodes.length; i++) {
    var node = section.childNodes[i];

    if (node.className && node.className == "section-body") {
       return node;
    }
 }
 return null;
}

function expand_section(section) {
 var sectionBody = get_section_body(section);

 sectionBody.style.height = "auto";
 var height = sectionBody.clientHeight;
 sectionBody.style.height = "0px";
 section.className = "section section-active";

 setTimeout(function() {
    sectionBody.style.height = "" + height + "px";
    setTimeout(function(){
       //Set this because I want so
       sectionBody.style.height="auto";
    }, 550);

 }, 50);

}

function collapse_section(section) {
 var sectionBody = get_section_body(section);
 sectionBody.style.height = "" + sectionBody.clientHeight + "px";
 section.className = "section";
 setTimeout(function() {
    sectionBody.style.height = "0px";
 }, 50);
}

function activate_section(section_header) {
 var section_to_activate = section_header.parentNode;
 if (section_to_activate.className.indexOf("section-active") >= 0) {
    collapse_section(section_to_activate);
    return;
 }

 var accordeon = section_to_activate.parentNode;

 for (var i = 0; i < accordeon.childNodes.length; i++) {
    var section = accordeon.childNodes[i];

    if (section.className && section.className.indexOf("section-active") >= 0) {
       //section.className = "section";
       collapse_section(section);
    }
 }

 //section_to_activate.className = "section section-active";
 expand_section(section_to_activate);
}
