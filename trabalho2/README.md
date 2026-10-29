# Trabalho 2 - Recomendação de Filme Netflix via Big Data

**Disciplina:** Big Data  
**Data:** Outubro 2026  
**Objetivo:** Criar um filme hipotético baseado em análises estatísticas do dataset Netflix utilizando Hadoop MapReduce

---

## 📋 Visão Geral

Este projeto analisa o catálogo completo de filmes da Netflix através de múltiplos jobs MapReduce para extrair padrões e tendências. Com base nessas análises, gera uma recomendação fundamentada em dados de um filme hipotético, incluindo:

- 🎬 Título
- ⏱️ Duração
- 🎭 Gênero
- 👥 Elenco
- 🎥 Diretor
- 🌍 País de origem
- 📝 Descrição

---

## 🏗️ Arquitetura do Sistema

### Jobs MapReduce Implementados

O sistema executa **7 jobs MapReduce** em sequência:

| Job | Análise | Objetivo |
|-----|---------|----------|
| **1** | **Gêneros** | Identificar os gêneros mais populares no catálogo de filmes |
| **2** | **Duração** | Calcular estatísticas de duração (média, mínimo, máximo, mediana) |
| **3** | **Elenco** | Listar atores mais frequentes (mínimo 3 aparições) |
| **4** | **Diretores** | Identificar diretores mais prolíficos (mínimo 2 filmes) |
| **5** | **Palavras-Chave** | Extrair termos mais comuns em descrições de filmes |
| **6** | **Países** | Determinar principais países produtores de conteúdo |
| **7** | **Recomendação Final** | Consolidar todas as análises e gerar o filme hipotético |

### Estrutura de Diretórios

```
C:\netflix-hadoop\trabalho2\
├── input\
│   └── netflix_titles.csv          # Dataset original
├── src\
│   └── main\
│       └── java\
│           └── com\hadoop\netflix\
│               ├── analysis\       # Mappers e Reducers de análise (12 arquivos)
│               │   ├── GenreAnalysisMapper.java
│               │   ├── GenreAnalysisReducer.java
│               │   ├── DurationAnalysisMapper.java
│               │   ├── DurationAnalysisReducer.java
│               │   ├── CastAnalysisMapper.java
│               │   ├── CastAnalysisReducer.java
│               │   ├── DirectorAnalysisMapper.java
│               │   ├── DirectorAnalysisReducer.java
│               │   ├── KeywordAnalysisMapper.java
│               │   ├── KeywordAnalysisReducer.java
│               │   ├── CountryAnalysisMapper.java
│               │   └── CountryAnalysisReducer.java
│               ├── recommendation\  # Geração da recomendação (2 arquivos)
│               │   ├── MovieRecommendationMapper.java
│               │   └── MovieRecommendationReducer.java
│               ├── MovieAnalysisDriver.java
│               ├── CsvParser.java
│               └── TextNormalizer.java
├── output\
│   └── final_recommendation\
│       └── part-r-00000            # 🎯 RESULTADO FINAL
├── output_temp\                    # Análises intermediárias
│   ├── genres\
│   ├── duration\
│   ├── cast\
│   ├── directors\
│   ├── keywords\
│   └── countries\
├── executar.bat                    # Script de execução
├── pom.xml
└── README.md
```

---

## 🚀 Como Executar

### Pré-requisitos

- ✅ Java JDK 11 ou superior instalado em `C:\Java\jdk-25.0.4.1`
- ✅ Hadoop 3.x instalado em `C:\hadoop-3.5.0`
- ✅ Dataset `netflix_titles.csv` na pasta `input\`
- ✅ `winutils.exe` instalado no Hadoop (`C:\hadoop-3.5.0\bin\winutils.exe`)

### Execução

**Opção 1: Via Script (Recomendado)**

Abra o **Prompt de Comando (CMD)** e execute:

```cmd
cd C:\netflix-hadoop\trabalho2
executar.bat
```

O script irá:
1. ✅ Compilar todos os arquivos Java
2. ✅ Criar o arquivo JAR
3. ✅ Executar os 7 jobs MapReduce sequencialmente
4. ✅ Exibir o resultado final formatado

**Opção 2: Execução Manual**

```cmd
cd C:\netflix-hadoop\trabalho2

:: Compilar
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -cp "%HADOOP_HOME%\share\hadoop\common\*;%HADOOP_HOME%\share\hadoop\mapreduce\*" -d build @sources.txt

:: Criar JAR
cd build
jar cf ..\netflix-trabalho2.jar *
cd ..

:: Executar
hadoop jar netflix-trabalho2.jar com.hadoop.netflix.MovieAnalysisDriver input/netflix_titles.csv output
```

### Visualizando o Resultado

Após a conclusão, visualize a recomendação:

```cmd
type output\final_recommendation\part-r-00000
```

---

## 📊 Metodologia de Análise

### 1. Coleta de Dados

Cada job MapReduce lê o dataset completo (`netflix_titles.csv`) e filtra apenas registros do tipo **Movie** (excluindo séries).

### 2. Processamento Distribuído

- **Fase Map**: Extrai informações relevantes (gêneros, atores, durações, etc.)
- **Fase Reduce**: Agrega dados, calcula frequências e estatísticas

### 3. Geração da Recomendação

O job final consolida os resultados de todas as análises e aplica a seguinte lógica:

#### Seleção de Gênero
- Escolhe os **Top 3 gêneros** mais populares
- Justificativa: Alinhamento com preferências demonstradas do público

#### Definição de Duração
- Utiliza a **duração média** dos filmes no catálogo
- Considera a mediana para evitar distorções de outliers

#### Escolha do Elenco
- Seleciona **5-6 atores** mais frequentes
- Filtro mínimo: 3 aparições no catálogo
- Justificativa: Histórico comprovado na plataforma

#### Escolha do Diretor
- Seleciona o **diretor mais prolífico**
- Filtro mínimo: 2 filmes dirigidos
- Justificativa: Experiência e consistência

#### Geração da Descrição
- Utiliza as **10 palavras-chave** mais frequentes em descrições
- Estrutura narrativa: Setup → Conflito → Stakes
- Comprimento: 25-35 palavras (padrão Netflix)

#### Seleção de País
- Escolhe o **país com maior produção** no catálogo
- Justificativa: Alinhamento com mercado estabelecido

---

## 📈 Exemplo de Resultado

```
╔═══════════════════════════════════════════════════════════════════════════╗
║                    RECOMENDAÇÃO DE FILME NETFLIX                          ║
║                  Baseado em Análise de Big Data (Hadoop)                  ║
╚═══════════════════════════════════════════════════════════════════════════╝

┌───────────────────────────────────────────────────────────────────────────┐
│ DETALHES DO FILME                                                         │
└───────────────────────────────────────────────────────────────────────────┘

  🎬 TÍTULO        : The Life Young
  ⏱️  DURAÇÃO      : 100 min
  🎭 GÊNERO        : International Movies, Dramas, Comedies
  🌍 PAÍS          : United States
  📅 ANO LANÇAMENTO: 2026
  
  👥 ELENCO        : Anupam Kher, Shah Rukh Khan, Naseeruddin Shah, 
                     Akshay Kumar, Om Puri
  
  🎥 DIRETOR       : Rajiv Chilaka
  
  📝 DESCRIÇÃO     :
     When a young detective uncovers mysterious connections between 
     life and young, their life takes an unexpected turn. As dark 
     secrets emerge, they must navigate a dangerous world where new 
     is what it seems.

┌───────────────────────────────────────────────────────────────────────────┐
│ JUSTIFICATIVA BASEADA EM DADOS                                            │
└───────────────────────────────────────────────────────────────────────────┘

  ▸ SELEÇÃO DE GÊNERO
    Os gêneros escolhidos representam as categorias mais populares:
    1. International Movies (2,752 filmes)
    2. Dramas (2,427 filmes)
    3. Comedies (1,674 filmes)

  ▸ ANÁLISE DE DURAÇÃO
    Duração média de filmes: 100 minutos
    Faixa de variação: 3 - 312 min
    Duração mais comum (mediana): 98 min
    Total de filmes analisados: 6,127

  ▸ SELEÇÃO DO ELENCO
    Os atores selecionados possuem histórico comprovado:
    - Anupam Kher: 42 filmes na Netflix
    - Shah Rukh Khan: 35 filmes na Netflix
    - Naseeruddin Shah: 32 filmes na Netflix
    - Akshay Kumar: 30 filmes na Netflix
    - Om Puri: 30 filmes na Netflix

  ▸ ESCOLHA DO DIRETOR
    Rajiv Chilaka dirigiu 22 filmes no catálogo Netflix.
    Experiência e consistência tornam esta uma escolha fundamentada.

  ▸ PALAVRAS-CHAVE NA DESCRIÇÃO
    Principais palavras em descrições de filmes:
    - "life" (573 ocorrências)
    - "young" (558 ocorrências)
    - "new" (478 ocorrências)
    - "man" (452 ocorrências)
    - "family" (440 ocorrências)

  ▸ PAÍS E MERCADO
    United States possui 2,363 filmes no catálogo, representando
    um dos maiores mercados de produção.

╔═══════════════════════════════════════════════════════════════════════════╗
║ ÍNDICE DE CONFIANÇA: ████████████████████ 95/100                        ║
║ Baseado em popularidade do gênero, confiabilidade do elenco e tendências ║
╚═══════════════════════════════════════════════════════════════════════════╝
```

---

## 🔍 Análises Detalhadas

### Normalização de Texto

O sistema utiliza a classe `TextNormalizer` para processar descrições:
- ✅ Conversão para minúsculas
- ✅ Remoção de pontuação
- ✅ Remoção de stopwords (palavras comuns sem valor semântico)

### Filtros Aplicados

- **Filmes vs Séries**: Apenas filmes são analisados
- **Elenco mínimo**: Atores com pelo menos 3 aparições
- **Diretores mínimos**: Diretores com pelo menos 2 filmes
- **Palavras-chave**: Palavras com pelo menos 3 letras

---

## 🎓 Justificativa Acadêmica

### Por que este filme seria bem-sucedido?

1. **Alinhamento com Mercado**: Os gêneros escolhidos representam mais de 30% do catálogo total
2. **Elenco Comprovado**: Todos os atores têm histórico de múltiplas produções na plataforma
3. **Duração Otimizada**: A duração está dentro da faixa preferida pelo público (90-120 min)
4. **Vocabulário Testado**: A descrição utiliza termos que aparecem em milhares de filmes bem-sucedidos
5. **Mercado Estabelecido**: O país de origem é responsável por grande parte da produção total

### Limitações da Análise

- 📌 Não considera dados de audiência ou ratings
- 📌 Não analisa tendências temporais (séries históricas)
- 📌 Não considera orçamento ou viabilidade de produção
- 📌 Descrição gerada segue template básico

---

## 🛠️ Tecnologias Utilizadas

- **Hadoop MapReduce 3.5.0**: Framework de processamento distribuído
- **Java 11**: Linguagem de programação
- **Maven**: Gerenciamento de dependências (opcional)
- **Windows Batch**: Scripts de automação

---

## 📝 Estrutura do Dataset

O dataset `netflix_titles.csv` contém os seguintes campos:

| Campo | Descrição |
|-------|-----------|
| show_id | ID único do título |
| type | Movie ou TV Show |
| title | Nome do filme/série |
| director | Diretor(es) |
| cast | Elenco principal |
| country | País(es) de origem |
| date_added | Data de adição à Netflix |
| release_year | Ano de lançamento |
| rating | Classificação etária |
| duration | Duração (minutos para filmes, temporadas para séries) |
| listed_in | Gêneros |
| description | Sinopse |

---

## 🐛 Troubleshooting

### Erro de Compilação

Se houver erro de encoding:
```cmd
javac -encoding UTF-8 ...
```

### Hadoop não encontrado

Verifique as variáveis de ambiente no script `executar.bat`:
```cmd
set JAVA_HOME=C:\Java\jdk-25.0.4.1
set HADOOP_HOME=C:\hadoop-3.5.0
```

### Diretório de output já existe

O script limpa automaticamente. Para limpeza manual:
```cmd
rmdir /s /q output
rmdir /s /q output_temp
```

### Falta winutils.exe

Baixe de: https://github.com/cdarlint/winutils
Copie para: `C:\hadoop-3.5.0\bin\`

---

## 👨‍💻 Desenvolvimento

### Estrutura de Classes

#### Pacote `analysis`
Contém todos os Mappers e Reducers de análise específica (12 classes).

#### Pacote `recommendation`
Responsável pela consolidação final e geração da recomendação (2 classes).

#### Classes Utilitárias
- **CsvParser**: Parse de linhas CSV com suporte a vírgulas dentro de aspas
- **TextNormalizer**: Normalização e limpeza de texto

#### Driver Principal
- **MovieAnalysisDriver**: Coordena execução sequencial dos 7 jobs

---

## 📄 Arquivos de Saída

### Análises Intermediárias (output_temp/)

- `genres/part-r-00000` - Frequência de gêneros
- `duration/part-r-00000` - Estatísticas de duração
- `cast/part-r-00000` - Top atores (≥3 filmes)
- `directors/part-r-00000` - Top diretores (≥2 filmes)
- `keywords/part-r-00000` - Palavras-chave mais comuns
- `countries/part-r-00000` - Principais países produtores

### Resultado Final (output/)

- `final_recommendation/part-r-00000` - **Filme hipotético com justificativas**

---

## 🎯 Conclusão

Este projeto demonstra como análises de Big Data podem fundamentar decisões criativas na indústria do entretenimento. Através do processamento distribuído de grandes volumes de dados, foi possível identificar padrões claros que indicam características de conteúdo bem-sucedido na plataforma Netflix.

A recomendação gerada não é apenas uma sugestão aleatória, mas sim um filme hipotético cuidadosamente construído a partir de evidências estatísticas extraídas de milhares de títulos reais.

---

## 📊 Estatísticas do Projeto

- **Total de arquivos Java**: 17
- **Jobs MapReduce**: 7
- **Linhas de código**: ~2,500
- **Tempo médio de execução**: 2-3 minutos
- **Dataset processado**: 8,810 títulos
- **Filmes analisados**: 6,127

---

**Desenvolvido com Hadoop MapReduce | Outubro 2026**

**Autor:** Trabalho Prático de Big Data  
**Instituição:** [Nome da Instituição]
