IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$fromDate', '$toDate');
fR = filterByEvent(f2, 'tenant-destroyed');

tR = extractWs(fR);

result = FOREACH tR GENERATE TOTUPLE(TOTUPLE(ws));

