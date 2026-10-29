package com.hadoop.netflix.recommendation;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.*;

/**
 * Reducer que gera a recomendação final de filme baseado nas análises
 * Output com caracteres ASCII simples (sem UTF-8)
 */
public class MovieRecommendationReducer extends Reducer<Text, Text, Text, Text> {
    
    private Map<String, Integer> genres = new HashMap<>();
    private Map<String, Integer> cast = new HashMap<>();
    private Map<String, Integer> directors = new HashMap<>();
    private Map<String, Integer> countries = new HashMap<>();
    private Map<String, Integer> keywords = new HashMap<>();
    private String durationStats = "";

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) 
            throws IOException, InterruptedException {
        
        String category = key.toString();
        
        for (Text val : values) {
            String line = val.toString();
            String[] parts = line.split("\t");
            
            if (parts.length >= 2) {
                String name = parts[0];
                try {
                    if (category.equals("genres")) {
                        genres.put(name, Integer.parseInt(parts[1]));
                    } else if (category.equals("cast")) {
                        cast.put(name, Integer.parseInt(parts[1]));
                    } else if (category.equals("directors")) {
                        directors.put(name, Integer.parseInt(parts[1]));
                    } else if (category.equals("countries")) {
                        countries.put(name, Integer.parseInt(parts[1]));
                    } else if (category.equals("keywords")) {
                        keywords.put(name, Integer.parseInt(parts[1]));
                    } else if (category.equals("duration")) {
                        durationStats = parts[1];
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        StringBuilder output = new StringBuilder();
        
        // Cabeçalho
        output.append("===============================================================================\n");
        output.append("                    RECOMENDACAO DE FILME NETFLIX\n");
        output.append("                  Baseado em Analise de Big Data (Hadoop)\n");
        output.append("===============================================================================\n\n");

        // Top resultados
        List<Map.Entry<String, Integer>> topGenres = getTopN(genres, 3);
        List<Map.Entry<String, Integer>> topCast = getTopN(cast, 6);
        List<Map.Entry<String, Integer>> topDirectors = getTopN(directors, 1);
        List<Map.Entry<String, Integer>> topCountries = getTopN(countries, 1);
        List<Map.Entry<String, Integer>> topKeywords = getTopN(keywords, 10);

        // Parsear estatísticas de duração
        int avgDuration = 100;
        int minDuration = 0, maxDuration = 0, modeDuration = 0, countFilms = 0;
        if (!durationStats.isEmpty()) {
            String[] stats = durationStats.split("\\|");
            for (String stat : stats) {
                if (stat.startsWith("AVG=")) avgDuration = (int) Double.parseDouble(stat.substring(4));
                if (stat.startsWith("MIN=")) minDuration = Integer.parseInt(stat.substring(4));
                if (stat.startsWith("MAX=")) maxDuration = Integer.parseInt(stat.substring(4));
                if (stat.startsWith("MODE=")) modeDuration = Integer.parseInt(stat.substring(5));
                if (stat.startsWith("COUNT=")) countFilms = Integer.parseInt(stat.substring(6));
            }
        }

        String title = generateTitle(topKeywords);
        String description = generateDescription(topKeywords, topGenres);

        // Detalhes do filme
        output.append("-------------------------------------------------------------------------------\n");
        output.append("| DETALHES DO FILME                                                         |\n");
        output.append("-------------------------------------------------------------------------------\n\n");
        output.append("  * TITULO        : ").append(title).append("\n");
        output.append("  * DURACAO       : ").append(avgDuration).append(" min\n");
        
        output.append("  * GENERO        : ");
        for (int i = 0; i < topGenres.size(); i++) {
            output.append(topGenres.get(i).getKey());
            if (i < topGenres.size() - 1) output.append(", ");
        }
        output.append("\n");
        
        if (!topCountries.isEmpty()) {
            output.append("  * PAIS          : ").append(topCountries.get(0).getKey()).append("\n");
        }
        output.append("  * ANO LANCAMENTO: 2026\n\n");
        
        output.append("  * ELENCO        : ");
        for (int i = 0; i < Math.min(topCast.size(), 5); i++) {
            output.append(topCast.get(i).getKey());
            if (i < Math.min(topCast.size(), 5) - 1) output.append(", ");
        }
        output.append("\n\n");
        
        if (!topDirectors.isEmpty()) {
            output.append("  * DIRETOR       : ").append(topDirectors.get(0).getKey()).append("\n\n");
        }
        
        output.append("  * DESCRICAO     :\n");
        output.append("     ").append(description).append("\n\n");

        // Justificativas
        output.append("-------------------------------------------------------------------------------\n");
        output.append("| JUSTIFICATIVA BASEADA EM DADOS                                            |\n");
        output.append("-------------------------------------------------------------------------------\n\n");

        // Justificativa de gênero
        output.append("  > SELECAO DE GENERO\n");
        output.append("    Os generos escolhidos representam as categorias mais populares no\n");
        output.append("    catalogo de filmes da Netflix:\n");
        List<Map.Entry<String, Integer>> top5Genres = getTopN(genres, 5);
        for (int i = 0; i < top5Genres.size(); i++) {
            Map.Entry<String, Integer> entry = top5Genres.get(i);
            output.append(String.format("    %d. %s (%d filmes)\n", 
                i + 1, entry.getKey(), entry.getValue()));
        }
        output.append("\n");

        // Justificativa de duração
        output.append("  > ANALISE DE DURACAO\n");
        output.append(String.format("    Duracao media de filmes: %d minutos\n", avgDuration));
        output.append(String.format("    Faixa de variacao: %d - %d min\n", minDuration, maxDuration));
        output.append(String.format("    Duracao mais comum (mediana): %d min\n", modeDuration));
        output.append(String.format("    Total de filmes analisados: %d\n", countFilms));
        output.append("    Nossa escolha esta alinhada com o padrao do mercado.\n\n");

        // Justificativa de elenco
        output.append("  > SELECAO DO ELENCO\n");
        output.append("    Os atores selecionados possuem historico comprovado no catalogo:\n");
        for (int i = 0; i < Math.min(topCast.size(), 5); i++) {
            Map.Entry<String, Integer> actor = topCast.get(i);
            output.append(String.format("    - %s: %d filmes na Netflix\n", 
                actor.getKey(), actor.getValue()));
        }
        output.append("\n");

        // Justificativa de diretor
        output.append("  > ESCOLHA DO DIRETOR\n");
        if (!topDirectors.isEmpty()) {
            Map.Entry<String, Integer> director = topDirectors.get(0);
            output.append(String.format("    %s dirigiu %d filmes no catalogo Netflix.\n", 
                director.getKey(), director.getValue()));
            output.append("    Experiencia e consistencia tornam esta uma escolha fundamentada.\n");
        }
        output.append("\n");

        // Justificativa de palavras-chave
        output.append("  > PALAVRAS-CHAVE NA DESCRICAO\n");
        output.append("    Principais palavras em descricoes de filmes (frequencia):\n");
        for (int i = 0; i < Math.min(topKeywords.size(), 8); i++) {
            Map.Entry<String, Integer> keyword = topKeywords.get(i);
            output.append(String.format("    - \"%s\" (%d ocorrencias)\n", 
                keyword.getKey(), keyword.getValue()));
        }
        output.append("    Nossa descricao incorpora estes termos de engajamento comprovado.\n\n");

        // Justificativa de país
        output.append("  > PAIS E MERCADO\n");
        if (!topCountries.isEmpty()) {
            Map.Entry<String, Integer> country = topCountries.get(0);
            int percentage = (country.getValue() * 100) / Math.max(countFilms, 1);
            output.append(String.format("    %s possui %d filmes no catalogo (~%d%% do mercado),\n", 
                country.getKey(), country.getValue(), percentage));
            output.append("    representando um dos maiores mercados de producao.\n");
        }
        output.append("\n");

        // Índice de confiança
        int confidence = calculateConfidence(topGenres, topCast, topDirectors);
        String bar = generateProgressBar(confidence);
        output.append("===============================================================================\n");
        output.append(String.format("| INDICE DE CONFIANCA: %s %d/100                        |\n", bar, confidence));
        output.append("| Baseado em popularidade do genero, confiabilidade do elenco e tendencias |\n");
        output.append("===============================================================================\n");

        // Escrever output
        context.write(new Text(""), new Text(output.toString()));
    }

    private String generateTitle(List<Map.Entry<String, Integer>> keywords) {
        if (keywords.size() >= 2) {
            String word1 = capitalize(keywords.get(0).getKey());
            String word2 = capitalize(keywords.get(1).getKey());
            return String.format("The %s %s", word1, word2);
        }
        return "The Last Journey";
    }

    private String generateDescription(List<Map.Entry<String, Integer>> keywords, 
                                       List<Map.Entry<String, Integer>> genres) {
        StringBuilder desc = new StringBuilder();
        
        desc.append("When a young detective uncovers mysterious connections between ");
        if (keywords.size() > 0) {
            desc.append(keywords.get(0).getKey()).append(" and ");
        }
        if (keywords.size() > 1) {
            desc.append(keywords.get(1).getKey());
        } else {
            desc.append("truth");
        }
        desc.append(", their life takes an unexpected turn. ");
        desc.append("As dark secrets emerge, they must navigate a dangerous world where ");
        if (keywords.size() > 2) {
            desc.append(keywords.get(2).getKey());
        } else {
            desc.append("nothing");
        }
        desc.append(" is what it seems.");
        
        return desc.toString();
    }

    private int calculateConfidence(List<Map.Entry<String, Integer>> genres,
                                    List<Map.Entry<String, Integer>> cast,
                                    List<Map.Entry<String, Integer>> directors) {
        int score = 60;
        if (!genres.isEmpty() && genres.get(0).getValue() > 100) score += 10;
        if (cast.size() >= 5) score += 10;
        if (!directors.isEmpty() && directors.get(0).getValue() >= 3) score += 10;
        if (!genres.isEmpty() && genres.get(0).getValue() > 500) score += 10;
        return Math.min(score, 95);
    }

    private String generateProgressBar(int percentage) {
        int filled = (percentage * 20) / 100;
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < filled; i++) bar.append("#");
        for (int i = filled; i < 20; i++) bar.append("-");
        return bar.toString();
    }

    private String capitalize(String word) {
        if (word == null || word.isEmpty()) return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    private List<Map.Entry<String, Integer>> getTopN(Map<String, Integer> map, int n) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return list.subList(0, Math.min(n, list.size()));
    }
}
