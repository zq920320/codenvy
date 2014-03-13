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

IMPORT 'macros.pig';

l = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

-- events with NULL parameters
a1 = FILTER l BY INDEXOF(message, '#null#', 0) >= 0;
a = FOREACH a1 GENERATE message;

-- events with EMPTY parameters
b1 = FILTER l BY INDEXOF(message, '##', 0) >= 0;
b2 = removeEvent(b1, 'user-update-profile');
b = FOREACH b2 GENERATE message;

-- events without USER and WORKSPACE context
c1 = FILTER l BY INDEXOF(message, '[][][]', 0) >= 0;
c2 = removeEvent(c1, 'tenant-created,tenant-destroyed,tenant-started,tenant-stopped,user-sso-logged-in,user-sso-logged-out,user-created,user-removed,user-added-to-ws,user-invite');
c3 = removeEvent(c2, 'user-changed-name,factory-url-accepted,user-update-profile');
c4 = FILTER c3 BY INDEXOF(message, 'WS#', 0) < 0 OR INDEXOF(message, 'USER#', 0) < 0;
c = FOREACH c4 GENERATE message;

d = UNION a, b, c;

result = DISTINCT d;
