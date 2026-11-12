@echo off
cd /d "%~dp0"
set "JAVA_HOME=C:\Users\USUARIO\.jdks\temurin-21.0.12.1"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo Compilando Clases del Gremio... (la primera vez tarda varios minutos)
echo El registro completo queda en build_log.txt
call gradlew.bat build --console=plain > build_log.txt 2>&1
set RESULT=%ERRORLEVEL%
echo CODIGO_SALIDA=%RESULT%>> build_log.txt
if %RESULT%==0 (
  echo.
  echo LISTO. El mod esta en build\libs\clases_gremio-0.1.0.jar
) else (
  echo.
  echo Hubo un error. Revisa build_log.txt
)
pause
