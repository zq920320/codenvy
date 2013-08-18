IMPORT 'macros.pig';

a1 = loadResources('$LOG');
a2 = filterByDate(a1, '$FROM_DATE', '$TO_DATE');
a3 = filterByEvent(a2, '$EVENT');
a4 = extractParam(a3, '$PARAM', param);
a = FOREACH a4 GENERATE param, event;

b1 = GROUP a BY param;
result = FOREACH b1 GENERATE group, COUNT(a);
