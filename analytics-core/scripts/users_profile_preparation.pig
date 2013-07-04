IMPORT 'macros.pig';

f = loadResources('$log');

a1 = filterByDate(f, '$FROM_DATE', '$TO_DATE');
a2 = filterByEvent(a1, 'user-update-profile');
a3 = extractUser(a2);
a4 = smartExtractParam(a3, 'FIRSTNAME', 'firstName');
a5 = smartExtractParam(a4, 'LASTNAME', 'lastName');
a6 = smartExtractParam(a5, 'COMPANY', 'company');
a = FOREACH a6 GENERATE user, firstName, lastName, company, MilliSecondsBetween(dt, ToDate('2010-01-01', 'yyyy-MM-dd')) AS delta;

b = lastUserProfileUpdate(a);

r1 = GROUP b BY user;
r = FOREACH r1 {
    t = FOREACH b GENERATE TOTUPLE(TOTUPLE(user), TOTUPLE(firstName), TOTUPLE(lastName), TOTUPLE(company));
    GENERATE group, t;
    }

result = FOREACH r GENERATE group, t;

