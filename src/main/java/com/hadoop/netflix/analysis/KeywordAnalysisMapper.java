package com.hadoop.netflix.analysis;

import com.hadoop.netflix.CsvParser;
import com.hadoop.netflix.TextNormalizer;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.List;

/**
 * Mapper para análise de palavras-chave em descrições de filmes
 */
public class KeywordAnalysisMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    private final Text wordKey = new Text();
    private final IntWritable one = new IntWritable(1);
    private TextNormalizer normalizer;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        normalizer = new TextNormalizer();
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString().trim();
        
        if (line.isEmpty() || line.startsWith("show_id,") || line.startsWith("show_id\t")) {
            return;
        }

        List<String> fields = CsvParser.parseLine(line);
        if (fields.size() < 12) {
            return;
        }

        String type = fields.get(1).replace("\"", "").trim();
        String description = fields.get(11).replace("\"", "").trim();

        // Filtrar apenas filmes
        if (!type.equalsIgnoreCase("Movie")) {
            return;
        }

        // Normalizar e extrair palavras
        List<String> words = normalizer.normalize(description);
        for (String word : words) {
            if (word.length() >= 3) { // Palavras com pelo menos 3 letras
                wordKey.set(word);
                context.write(wordKey, one);
            }
        }
    }
}
