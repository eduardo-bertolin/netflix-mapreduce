package com.hadoop.netflix.analysis;

import com.hadoop.netflix.CsvParser;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.List;

/**
 * Mapper para análise de elenco
 * Extrai e conta frequência de cada ator em filmes
 */
public class CastAnalysisMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    private final Text actorKey = new Text();
    private final IntWritable one = new IntWritable(1);

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
        String cast = fields.get(4).replace("\"", "").trim();

        // Filtrar apenas filmes
        if (!type.equalsIgnoreCase("Movie")) {
            return;
        }

        // Processar cada ator (separados por vírgula)
        if (!cast.isEmpty()) {
            String[] actors = cast.split(",");
            for (String actor : actors) {
                String cleanActor = actor.trim();
                if (!cleanActor.isEmpty()) {
                    actorKey.set(cleanActor);
                    context.write(actorKey, one);
                }
            }
        }
    }
}
