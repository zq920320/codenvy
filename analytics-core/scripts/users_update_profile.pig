IMPORT 'macros.pig';

l = loadResources('$log');

a1 = filterByEvent(l, 'user-update-profile');
a2 = filterByDate(a1, '$FROM_DATE', '$TO_DATE');

a3 = extractUser(a2);
a4 = extractParam(a3, 'FIRSTNAME', 'firstName');
a5 = extractParam(a4, 'LASTNAME', 'lastName');
a6 = extractParam(a5, 'COMPANY', 'company');
a7 = extractParam(a6, 'PHONE', 'phone');
a8 = extractParam(a7, 'JOBTITLE', 'job');
a = FOREACH a8 GENERATE user, firstName, lastName, company, phone, job;

r1 = FILTER a BY firstName != '' AND firstName != 'null' AND firstName IS NOT NULL AND
		lastName != '' AND lastName != 'null' AND lastName IS NOT NULL AND 
		company != '' AND company != 'null' AND company IS NOT NULL AND
		phone != '' AND phone != 'null' AND phone IS NOT NULL AND
		job != '' AND job != 'null' AND job IS NOT NULL;

result = FOREACH r1 GENERATE TOTUPLE(TOTUPLE(user), TOTUPLE(firstName), TOTUPLE(lastName), TOTUPLE(company), TOTUPLE(phone), TOTUPLE(job));

