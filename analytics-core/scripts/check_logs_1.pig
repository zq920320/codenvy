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

DEFINE keepOneInstance(X) RETURNS Y {
    x1 = GROUP $X BY event;
    $Y = FOREACH x1 {
	result = LIMIT $X 1;
	GENERATE group AS event, result AS message;
    }
};

lR = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

a1 = FILTER lR BY INDEXOF(message, '#null#', 0) >= 0;
a = keepOneInstance(a1);

b1 = FILTER lR BY INDEXOF(message, '##', 0) >= 0;
b2 = removeEvent(b1, 'user-update-profile,factory-url-accepted');
b = keepOneInstance(b2);

c1 = FILTER lR BY INDEXOF(message, '[][][]', 0) >= 0;
c21 = removeEvent(c1, 'tenant-created,tenant-destroyed,tenant-started,tenant-stopped,user-sso-logged-in,user-sso-logged-out,user-created,user-removed,user-added-to-ws,user-invite');
c2 = removeEvent(c21, 'user-changed-name,factory-url-accepted,user-update-profile');
c3 = FILTER c2 BY INDEXOF(message, 'WS#', 0) < 0 OR INDEXOF(message, 'USER#', 0) < 0;
c = keepOneInstance(c3);

r1 = UNION a, b, c;
result = FOREACH r1 GENERATE TOTUPLE(TOTUPLE(event), TOTUPLE(message));


