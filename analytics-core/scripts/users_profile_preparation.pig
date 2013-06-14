IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$FROM_DATE', '$TO_DATE');
f = prepareUserProfiles(f2);

r1 = GROUP f BY user;
r = FOREACH r1 {
    t = FOREACH f GENERATE TOTUPLE(TOTUPLE(firstName), TOTUPLE(lastName), TOTUPLE(company));
    GENERATE group, t;
    }

result = FOREACH r GENERATE group, t;

