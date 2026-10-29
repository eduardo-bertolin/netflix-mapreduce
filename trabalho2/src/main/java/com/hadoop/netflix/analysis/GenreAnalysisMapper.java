package com.hadoop.netflix.analysis;

import com.hadoop.netflix.CsvParser;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.List;

/**
 * Mapper para análise de gêneros
 * Extrai e conta frequência de cada gênero em filmes
 */
public class GenreAnalysisMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    private final Text genreKey = new Text();
    private final IntWritable one = new IntWritable(1);

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString().trim();
        
        // Pular cabeçalho
        if (line.isEmpty() || line.startsWith("show_id,") || line.startsWith("show_id\t")) {
            return;
        }

        List<String> fields = CsvParser.parseLine(line);
        if (fields.size() < 12) {
            return;
        }

        String type = fields.get(1).replace("\"", "").trim();
        String listedIn = fields.get(10).replace("\"", "").trim();

        // Filtrar apenas filmes
        if (!type.equalsIgnoreCase("Movie")) {
            return;
        }

        // Processar cada gênero (separados por vírgula)
        if (!listedIn.isEmpty()) {
            String[] genres = listedIn.split(",");
            for (String genre : genres) {
                String cleanGenre = genre.trim();
                if (!cleanGenre.isEmpty()) {
                    genreKey.set(cleanGenre);
                    context.write(genreKey, one);
                }
            }
        }
    }
}
