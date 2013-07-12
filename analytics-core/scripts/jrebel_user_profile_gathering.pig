IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$FROM_DATE', '$TO_DATE');
fR = filterByEvent(f2, 'jrebel-user-profile-info');

t1 = extractUser(fR);
t2 = extractParam(t1, 'FIRSTNAME', 'firstname');
t3 = extractParam(t2, 'LASTNAME', 'lastname');
tR = extractParam(t3, 'PHONE', 'phone');

result = FOREACH tR GENERATE TOTUPLE(TOTUPLE(user), TOTUPLE(firstname), TOTUPLE(lastname), TOTUPLE(phone));

