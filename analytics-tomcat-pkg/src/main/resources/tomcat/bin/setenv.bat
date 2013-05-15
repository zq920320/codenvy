@ECHO OFF
REM Environment Variable Prerequisites

REM Global JAVA options
SET JAVA_OPTS=-Xms256m -Xmx2048m -XX:MaxPermSize=256m

REM Sets some variables
SET SECURITY_OPTS=-Djava.security.auth.login.config=%CATALINA_HOME%/conf/jaas.conf

SET ANALYTICS_OPTS=-Danalytics.logs.directory=C:/cygwin/home/tolusha/pig/logs/logs-production
SET ANALYTICS_OPTS=%ANALYTICS_OPTS% -Danalytics.scripts.directory=%CATALINA_HOME%/scripts
SET ANALYTICS_OPTS=%ANALYTICS_OPTS% -Danalytics.result.directory=%CATALINA_HOME%/data/results
SET ANALYTICS_OPTS=%ANALYTICS_OPTS% -Danalytics.metrics.initial.values=%CATALINA_HOME%/analytics-conf/initial-values.xml
SET ANALYTICS_OPTS=%ANALYTICS_OPTS% -Danalytics.acton.ftp.properties=%CATALINA_HOME%/analytics-conf/acton-ftp.properties
REM SET ANALYTICS_OPTS=%ANALYTICS_OPTS% -Danalytics.acton.cron.scheduling="0 0 1 ? * *"
SET ANALYTICS_OPTS=%ANALYTICS_OPTS% -Dcom.codenvy.analytics.logpath=%CATALINA_HOME%/logs
SET ANALYTICS_OPTS=%ANALYTICS_OPTS% -Dorganization.application.server.url=


SET QUARTZ_OPTS=-Dorg.terracotta.quartz.skipUpdateCheck=true

SET JMX_OPTS=-Dcom.sun.management.jmxremote.authenticate=true
SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.password.file=%CATALINA_HOME%/conf/jmxremote.password
SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.access.file=%CATALINA_HOME%/conf/jmxremote.access
SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.ssl=false

REM uncomment if you want to debug app server
REM REMOTE_DEBUG=-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y
SET REMOTE_DEBUG=

SET JAVA_OPTS=%JAVA_OPTS% %SECURITY_OPTS% %ANALYTICS_OPTS% %JMX_OPTS% %REMOTE_DEBUG% %QUARTZ_OPTS%

SET CLASSPATH=%CATALINA_HOME%/conf/;%CATALINA_HOME%/lib/jul-to-slf4j.jar
SET CLASSPATH=%CLASSPATH%;%CATALINA_HOME%/lib/slf4j-api.jar
SET CLASSPATH=%CLASSPATH%;%CATALINA_HOME%/lib/logback-classic.jar
SET CLASSPATH=%CLASSPATH%;%CATALINA_HOME%/lib/logback-core.jar
SET CLASSPATH=%CLASSPATH%;%CATALINA_HOME%/lib/mail.jar

echo =======
echo %JAVA_OPT%
echo =======