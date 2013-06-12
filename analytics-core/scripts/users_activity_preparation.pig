IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$fromDate', '$toDate');
f3 = extractUser(f2);
f = FOREACH f3 GENERATE user, message;

r1 = GROUP f BY user;
r = FOREACH r1 {
    t = FOREACH f GENERATE message;
    GENERATE group, t;
    }

result = FOREACH r GENERATE group, t;
