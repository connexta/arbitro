@echo off
set BALANA_CLASSPATH=.\conf
FOR %%C in ("\lib\*.jar") DO set BALANA_CLASSPATH=!BALANA_CLASSPATH!;".\lib\%%~nC%%~xC"
FOR %%D in ("\..\..\balana-core\target\*.jar") DO set BALANA_CLASSPATH=!BALANA_CLASSPATH!;".\..\..\arbitro-core\target\%%~nD%%~xD"
FOR %%E in ("\target\*.jar") DO set BALANA_CLASSPATH=!BALANA_CLASSPATH!;".\target\%%~nE%%~xE"
set _RUNJAVA="%JAVA_HOME%\bin\java"
%_RUNJAVA% -cp "%BALANA_CLASSPATH%" com.connexta.arbitro.samples.image.filtering.ImageFilter %*

