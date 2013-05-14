IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$fromDate', '$toDate');
fR = filterByEvent(f2, 'user-created');

tR = extractUserFromAliases(fR);

result = FOREACH tR GENERATE user;

