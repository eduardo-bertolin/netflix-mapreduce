@echo off
setlocal enabledelayedexpansion
cls
echo.
echo ===============================================================
echo    TRABALHO 2 - ANALISE NETFLIX E RECOMENDACAO DE FILME
echo           Processamento via Hadoop MapReduce
echo ===============================================================
echo.

set "JAVA_HOME=C:\Java\jdk-25.0.4.1"
set "HADOOP_HOME=C:\hadoop-3.5.0"
set "PATH=%JAVA_HOME%\bin;%HADOOP_HOME%\bin;%PATH%"

cd /d C:\netflix-hadoop\trabalho2

echo [1/4] Limpando builds anteriores...
if exist build rmdir /s /q build 2>nul
mkdir build 2>nul

echo.
echo [2/4] Compilando arquivos Java...
dir /s /b src\*.java > sources.txt 2>nul

"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -cp "%HADOOP_HOME%\share\hadoop\common\*;%HADOOP_HOME%\share\hadoop\mapreduce\*" -d build @sources.txt

if errorlevel 1 (
    echo.
    echo [ERRO] Falha na compilacao!
    del sources.txt 2>nul
    pause
    exit /b 1
)
del sources.txt
echo [OK] Compilacao concluida!

echo.
echo [3/4] Criando arquivo JAR...
cd build
"%JAVA_HOME%\bin\jar.exe" cf ..\netflix-trabalho2.jar *
cd ..
if exist build rmdir /s /q build
echo [OK] JAR criado!

echo.
echo [4/4] Preparando ambiente de execucao...
if not exist tmp mkdir tmp

echo Limpando diretorios antigos...
timeout /t 1 /nobreak >nul 2>&1

if exist output_temp (
    rmdir /s /q output_temp 2>nul
    if exist output_temp rd /s /q output_temp 2>nul
)

if exist output (
    rmdir /s /q output 2>nul
    if exist output rd /s /q output 2>nul
)

timeout /t 1 /nobreak >nul 2>&1
echo [OK] Ambiente preparado!

echo.
echo ================================================================
echo   INICIANDO PROCESSAMENTO HADOOP - AGUARDE...
echo   Serao executados 7 jobs MapReduce em sequencia
echo ================================================================
echo.

hadoop jar netflix-trabalho2.jar com.hadoop.netflix.MovieAnalysisDriver -fs file:/// -Dhadoop.tmp.dir=C:/netflix-hadoop/trabalho2/tmp input/netflix_titles.csv output

if errorlevel 1 (
    echo.
    echo [ERRO] Falha durante o processamento Hadoop!
    echo Verifique os logs acima para mais detalhes.
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
    echo Verifique o diretorio output\
)

echo.
echo.
echo ================================================================
echo   Arquivos gerados:
echo   - output\final_recommendation\part-r-00000 (RESULTADO FINAL)
echo   - output_temp\genres\part-r-00000 (Analise de generos)
echo   - output_temp\duration\part-r-00000 (Analise de duracao)
echo   - output_temp\cast\part-r-00000 (Analise de elenco)
echo   - output_temp\directors\part-r-00000 (Analise de diretores)
echo   - output_temp\keywords\part-r-00000 (Analise de palavras-chave)
echo   - output_temp\countries\part-r-00000 (Analise de paises)
echo ================================================================
echo.

endlocal
pause
