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

echo [1/5] Limpando builds anteriores...
if exist build rmdir /s /q build 2>nul
mkdir build 2>nul

echo.
echo [2/5] Compilando arquivos Java...
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
echo [3/5] Criando arquivo JAR...
cd build
"%JAVA_HOME%\bin\jar.exe" cf ..\netflix-trabalho2.jar *
cd ..
if exist build rmdir /s /q build
echo [OK] JAR criado!

echo.
echo [4/5] Limpando diretorios de output (FORCADO)...
echo IMPORTANTE: Fechando processos que possam estar usando os arquivos...

REM Tentar deletar multiplas vezes com intervalos
for %%d in (output output_temp) do (
    if exist %%d (
        echo Removendo %%d...
        rmdir /s /q %%d 2>nul
        timeout /t 1 /nobreak >nul 2>&1
        if exist %%d (
            rd /s /q %%d 2>nul
            timeout /t 1 /nobreak >nul 2>&1
        )
        if exist %%d (
            echo [AVISO] Nao foi possivel remover completamente %%d
            echo Tentando remover conteudo interno...
            del /f /s /q %%d\*.* 2>nul
            for /d %%i in (%%d\*) do rmdir /s /q "%%i" 2>nul
        )
    )
)

if not exist tmp mkdir tmp
echo [OK] Limpeza concluida!

echo.
echo [5/5] Verificacao final...
if exist output (
    echo [ERRO CRITICO] O diretorio 'output' ainda existe!
    echo.
    echo SOLUCAO MANUAL:
    echo 1. Feche este terminal
    echo 2. Delete manualmente as pastas 'output' e 'output_temp'
    echo 3. Execute o script novamente
    echo.
    pause
    exit /b 1
)

if exist output_temp (
    echo [ERRO CRITICO] O diretorio 'output_temp' ainda existe!
    echo.
    echo SOLUCAO MANUAL:
    echo 1. Feche este terminal
    echo 2. Delete manualmente as pastas 'output' e 'output_temp'
    echo 3. Execute o script novamente
    echo.
    pause
    exit /b 1
)

echo [OK] Ambiente preparado para execucao!

echo.
echo ================================================================
echo   INICIANDO PROCESSAMENTO HADOOP - AGUARDE...
echo   Serao executados 7 jobs MapReduce em sequencia
echo ================================================================
echo.

hadoop jar netflix-trabalho2.jar com.hadoop.netflix.MovieAnalysisDriver input/netflix_titles.csv output

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
