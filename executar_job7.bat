@echo off
setlocal enabledelayedexpansion
cls
echo.
echo ===============================================================
echo    EXECUTANDO APENAS JOB 7 - RECOMENDACAO FINAL
echo ===============================================================
echo.

set "JAVA_HOME=C:\Java\jdk-25.0.4.1"
set "HADOOP_HOME=C:\hadoop-3.5.0"
set "PATH=%JAVA_HOME%\bin;%HADOOP_HOME%\bin;%PATH%"

cd /d C:\netflix-hadoop

echo [1/3] Compilando arquivos Java...
if exist build rmdir /s /q build 2>nul
mkdir build

dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -cp "%HADOOP_HOME%\share\hadoop\common\*;%HADOOP_HOME%\share\hadoop\mapreduce\*" -d build @sources.txt
if errorlevel 1 (
    echo [ERRO] Falha na compilacao!
    del sources.txt 2>nul
    pause
    exit /b 1
)
del sources.txt
echo [OK] Compilacao concluida!

echo.
echo [2/3] Criando arquivo JAR...
cd build
"%JAVA_HOME%\bin\jar.exe" cf ..\netflix-mapreduce.jar *
cd ..
rmdir /s /q build
echo [OK] JAR criado!

echo.
echo [3/3] Executando Job 7 - Recomendacao Final...
echo.

if not exist tmp mkdir tmp

hadoop jar netflix-mapreduce.jar com.hadoop.netflix.FinalRecommendationDriver -fs file:/// -Dhadoop.tmp.dir=C:/netflix-hadoop/tmp

if errorlevel 1 (
    echo.
    echo [ERRO] Falha no Job 7!
    pause
    exit /b 1
)

echo.
echo.
echo ===============================================================
echo                     RESULTADO FINAL
echo ===============================================================
echo.

if exist output\final_recommendation\part-r-00000 (
    type output\final_recommendation\part-r-00000
) else (
    echo [AVISO] Arquivo de resultado nao encontrado!
)

echo.
echo.
echo ===============================================================
echo   Arquivo gerado:
echo   - output\final_recommendation\part-r-00000
echo ===============================================================
echo.

endlocal
pause
