IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$fromDate', '$toDate');
fR = filterByEvent(f2, 'jrebel-user-profile-info');

t1 = extractUser(fR);
t2 = smartExtractParam(t1, 'FIRSTNAME', 'firstname');
t3 = smartExtractParam(t2, 'LASTNAME', 'lastname');
tR = smartExtractParam(t3, 'PHONE', 'phone');

result = FOREACH tR GENERATE TOTUPLE(TOTUPLE(user), TOTUPLE(firstname), TOTUPLE(lastname), TOTUPLE(phone));

