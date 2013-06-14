IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$FROM_DATE', '$TO_DATE');
fR = filterByEvent(f2, 'user-created');
tR = extractUserFromAliases(fR);

result = FOREACH tR GENERATE TOTUPLE(TOTUPLE(user));

