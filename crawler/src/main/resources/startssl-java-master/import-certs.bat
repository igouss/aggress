cd %~dp0
set KEYTOOL=%JAVA_HOME%\jre\bin\keytool
set KEYSTORE=%JAVA_HOME%\jre\lib\security\cacerts
set PASSWORD=changeit
echo "Importing StartSSL certificates into %KEYSTORE%"
"%KEYTOOL%" -import -trustcacerts -keystore "%KEYSTORE%" -storepass %PASSWORD% -noprompt -alias startcom.ca -file ca.crt
"%KEYTOOL%" -import -trustcacerts -keystore "%KEYSTORE%" -storepass %PASSWORD% -noprompt -alias startcom.ca.sub.class1 -file sub.class1.server.ca.crt
"%KEYTOOL%" -import -trustcacerts -keystore "%KEYSTORE%" -storepass %PASSWORD% -noprompt -alias startcom.ca.sub.class2 -file sub.class2.server.ca.crt
"%KEYTOOL%" -import -trustcacerts -keystore "%KEYSTORE%" -storepass %PASSWORD% -noprompt -alias startcom.ca.sub.class3 -file sub.class3.server.ca.crt
"%KEYTOOL%" -import -trustcacerts -keystore "%KEYSTORE%" -storepass %PASSWORD% -noprompt -alias startcom.ca.sub.class4 -file sub.class4.server.ca.crt
"%KEYTOOL%" -import -trustcacerts -keystore "%KEYSTORE%" -storepass %PASSWORD% -noprompt -alias certplus_class2 -file certplus_class2.der
