package com.hadoop.netflix.analysis;

import com.hadoop.netflix.CsvParser;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.List;

/**
 * Mapper para análise de duração de filmes
 * Extrai durações em minutos
 */
public class DurationAnalysisMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    private final Text durationKey = new Text("DURATION");
    private final IntWritable durationValue = new IntWritable();

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
        String duration = fields.get(9).replace("\"", "").trim();

        // Filtrar apenas filmes
        if (!type.equalsIgnoreCase("Movie")) {
            return;
        }

        // Extrair minutos (formato: "90 min")
        if (duration.contains("min")) {
            try {
                String[] parts = duration.split("\\s+");
                int minutes = Integer.parseInt(parts[0]);
                durationValue.set(minutes);
                context.write(durationKey, durationValue);
            } catch (NumberFormatException ignored) {
            }
        }
    }
}
