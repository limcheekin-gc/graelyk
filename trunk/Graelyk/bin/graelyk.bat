echo off
gant -f "%GRAELYK_HOME%\scripts\Graelyk.gant" -P "%GRAELYK_HOME%\scripts" -L "%GRAELYK_HOME%\lib" %~1 %~2