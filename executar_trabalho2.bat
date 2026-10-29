@echo off
setlocal enabledelayedexpansion
cls
echo.
echo ===============================================================
echo    TRABALHO 2 - ANALISE NETFLIX E RECOMENDACAO DE FILME
echo           Processamento via Hadoop MapReduce
echo ===============================================================
echo.

:: Configurar variaveis de ambiente
set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.4.1"
set "HADOOP_HOME=C:\hadoop-3.5.0"
set "PATH=%JAVA_HOME%\bin;%HADOOP_HOME%\bin;%PATH%"

cd /d C:\netflix-hadoop

echo [1/4] Limpando builds anteriores...
if exist build rmdir /s /q build 2>nul
mkdir build 2>nul

echo.
echo [2/4] Compilando arquivos Java...
dir /s /b src\*.java > sources.txt 2>nul

javac -encoding UTF-8 -cp "%HADOOP_HOME%\share\hadoop\common\*;%HADOOP_HOME%\share\hadoop\mapreduce\*" -d build @sources.txt

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
jar cf ..\netflix-mapreduce.jar *
cd ..
if exist build rmdir /s /q build
echo [OK] JAR criado!

echo.
echo [4/4] Preparando ambiente de execucao...
if not exist tmp mkdir tmp
if exist output_temp rmdir /s /q output_temp 2>nul
if exist output rmdir /s /q output 2>nul
echo [OK] Ambiente preparado!

echo.
echo ================================================================
echo   INICIANDO PROCESSAMENTO HADOOP
echo   NOTA: Se houver erro de JAVA_HOME, siga as instrucoes abaixo
echo ================================================================
echo.

:: Tentar executar Hadoop
call "%HADOOP_HOME%\bin\hadoop.cmd" jar netflix-mapreduce.jar com.hadoop.netflix.MovieAnalysisDriver input/netflix_titles.csv output

if errorlevel 1 (
    echo.
    echo ================================================================
    echo [ERRO] Problema ao executar Hadoop
    echo.
    echo SOLUCAO:
    echo 1. Abra: C:\hadoop-3.5.0\etc\hadoop\hadoop-env.cmd
    echo 2. Encontre a linha: set "JAVA_HOME=..."
    echo 3. Substitua por: set JAVA_HOME=C:\PROGRA~1\Java\jdk-25.0.4.1
    echo    (caminho curto 8.3, sem espacos)
    echo 4. Salve o arquivo e execute este script novamente
    echo.
    echo Alternativamente:
    echo - Copie o JDK para um caminho sem espacos (ex: C:\Java\jdk-25)
    echo - Atualize JAVA_HOME no hadoop-env.cmd para o novo caminho
    echo ================================================================
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
echo ================================================================
echo   Arquivos gerados:
echo   - output\final_recommendation\part-r-00000
echo   - output_temp\ (analises intermediarias)
echo ================================================================
echo.

endlocal
pause
