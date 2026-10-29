# Netflix MapReduce - Análise de Big Data

Repositório contendo dois projetos de análise do dataset Netflix usando Hadoop MapReduce.

## 📁 Estrutura do Repositório

```
netflix-mapreduce/
├── trabalho1/          # Primeiro trabalho: Análises básicas
└── trabalho2/          # Segundo trabalho: Recomendação de filme
```

## 🎯 Trabalho 1 - Análises Básicas

Análises estatísticas do catálogo Netflix:
- Contagem de filmes por país
- Análise de duração
- Top gêneros
- Diretores mais prolíficos

**Navegue para:** [trabalho1/README.md](trabalho1/README.md)

## 🎬 Trabalho 2 - Recomendação de Filme

Sistema completo de análise e geração de recomendação de filme hipotético baseado em dados reais do catálogo Netflix.

**Navegue para:** [trabalho2/README.md](trabalho2/README.md)

### Resultado do Trabalho 2

O Trabalho 2 analisa 6.127 filmes através de 7 jobs MapReduce e gera uma recomendação fundamentada em dados:

- **Filme gerado:** The Life Young (100 min)
- **Gênero:** International Movies, Dramas, Comedies
- **Confiança:** 95/100
- **Análises realizadas:** Gêneros, duração, elenco, diretores, palavras-chave, países

Ver resultado completo em: [trabalho2/RESULTADO_FINAL.txt](trabalho2/RESULTADO_FINAL.txt)

## 🚀 Requisitos

- Java JDK 11 ou superior
- Hadoop 3.x
- Windows (scripts .bat) ou adaptar para Linux/Mac
- Dataset: `netflix_titles.csv`

## 📊 Dataset

O dataset Netflix contém 8.810 títulos com informações sobre:
- Tipo (Movie/TV Show)
- Título, diretor, elenco
- País de origem
- Ano de lançamento
- Duração
- Gêneros
- Descrição

## 📝 Licença

Projeto acadêmico desenvolvido para a disciplina de Big Data.

---

**Desenvolvido com Hadoop MapReduce | Outubro 2026**