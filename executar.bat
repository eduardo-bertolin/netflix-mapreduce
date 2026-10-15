@echo off
echo =======================================================
echo EXECUTANDO JOB HADOOP MAPREDUCE - DATASET NETFLIX
echo =======================================================

set JAVA_HOME=C:\Program Files\Java\jdk-25.0.4.1
set HADOOP_HOME=C:\hadoop-3.5.0
set PATH=%JAVA_HOME%\bin;%HADOOP_HOME%\bin;%PATH%

cd /d C:\netflix-hadoop

if exist build rmdir /s /q build
mkdir build

:: 1. Compila os arquivos Java
echo [1/3] Compilando arquivos Java...
dir /s /b *.java > sources.txt
javac -cp "%HADOOP_HOME%\share\hadoop\common\*;%HADOOP_HOME%\share\hadoop\mapreduce\*" -d build @sources.txt
del sources.txt

:: 2. Cria o arquivo JAR
echo [2/3] Criando arquivo .jar com a estrutura de pacotes...
cd build
jar cf ..\netflix-mapreduce.jar *
cd ..
if exist build rmdir /s /q build

:: 3. Prepara diretórios do Hadoop
if not exist C:\netflix-hadoop\tmp mkdir C:\netflix-hadoop\tmp
if exist output_temp rmdir /s /q output_temp
if exist output rmdir /s /q output

:: 4. Executa o Job (Com os parâmetros corretos para o Windows e ToolRunner)
echo [3/3] Executando o Job MapReduce no Hadoop...
hadoop jar netflix-mapreduce.jar com.hadoop.netflix.NetflixDriver -fs file:/// -Dhadoop.tmp.dir=C:/netflix-hadoop/tmp input/netflix_titles.csv output

echo.
echo =======================================================
echo RESULTADO GERADO:
echo =======================================================
dir output
pause