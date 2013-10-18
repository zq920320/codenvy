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

f1 = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');
f = filterByEvent(f1, 'jrebel-user-profile-info');

t2 = extractParam(f, 'FIRSTNAME', 'firstname');
t3 = extractParam(t2, 'LASTNAME', 'lastname');
t = extractParam(t3, 'PHONE', 'phone');

result = FOREACH t GENERATE TOTUPLE(TOTUPLE(user), TOTUPLE(firstname), TOTUPLE(lastname), TOTUPLE(phone));

