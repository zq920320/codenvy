IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$fromDate', '$toDate');
fR = filterByEvent(f2, 'user-sso-logged-in');

t1 = extractUser(fR);
tR = extractParam(t1, 'USING', 'use');

result = FOREACH tR GENERATE TOTUPLE(TOTUPLE(user), TOTUPLE(use));

