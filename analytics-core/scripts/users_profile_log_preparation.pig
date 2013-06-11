IMPORT 'macros.pig';

f1 = loadResources('$log');
result = prepareUserProfiles(f1);

STORE result INTO '$resultDir/COMPANIES' USING PigStorage();

