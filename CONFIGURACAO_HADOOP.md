# Instruções de Configuração - Hadoop JAVA_HOME

## Problema Identificado

O Hadoop no Windows tem dificuldade com caminhos que contêm espaços, como `C:\Program Files\Java\jdk-25.0.4.1`.

## ✅ SOLUÇÃO 1: Usar Caminho Curto (8.3) - RECOMENDADO

1. Abra o Prompt de Comando (CMD) e execute:
```cmd
for %I in ("C:\Program Files\Java\jdk-25.0.4.1") do @echo %~sI
```

2. O comando retornará algo como: `C:\PROGRA~1\Java\JDK-25~1.1` ou similar

3. Edite o arquivo: `C:\hadoop-3.5.0\etc\hadoop\hadoop-env.cmd`

4. Encontre a linha (por volta da linha 25):
```cmd
set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.4.1"
```

5. Substitua pelo caminho curto obtido no passo 2:
```cmd
set JAVA_HOME=C:\PROGRA~1\Java\JDK-25~1.1
```
**IMPORTANTE:** Não use aspas quando usar caminho curto!

6. Salve o arquivo e execute: `executar_trabalho2.bat`

## ✅ SOLUÇÃO 2: Copiar Java para Caminho Sem Espaços

1. Copie a pasta do JDK para um local sem espaços:
```cmd
xcopy "C:\Program Files\Java\jdk-25.0.4.1" C:\Java\jdk-25 /E /I /H
```

2. Edite: `C:\hadoop-3.5.0\etc\hadoop\hadoop-env.cmd`

3. Altere para:
```cmd
set JAVA_HOME=C:\Java\jdk-25
```

4. Salve e execute: `executar_trabalho2.bat`

## ✅ SOLUÇÃO 3: Executar Manualmente (Temporário)

Se as soluções acima não funcionarem, execute os comandos manualmente:

```cmd
cd C:\netflix-hadoop

REM Definir variáveis
set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.4.1"
set "HADOOP_HOME=C:\hadoop-3.5.0"
set "PATH=%JAVA_HOME%\bin;%HADOOP_HOME%\bin;%PATH%"

REM Preparar ambiente
if exist output rmdir /s /q output
if exist output_temp rmdir /s /q output_temp

REM Executar Hadoop (em uma linha)
hadoop jar netflix-mapreduce.jar com.hadoop.netflix.MovieAnalysisDriver input/netflix_titles.csv output

REM Ver resultado
type output\final_recommendation\part-r-00000
```

## Verificação

Após aplicar uma das soluções, teste com:
```cmd
hadoop version
```

Deve exibir a versão do Hadoop sem erros.

---

**OBS:** O código Java já foi compilado com sucesso e o JAR `netflix-mapreduce.jar` está pronto. O problema é apenas na configuração do ambiente Hadoop.
