package com.hadoop.netflix.analysis;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * Reducer para análise de diretores
 * Soma filmes de cada diretor
 */
public class DirectorAnalysisReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    private final IntWritable result = new IntWritable();

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context) 
            throws IOException, InterruptedException {
        int sum = 0;
        for (IntWritable val : values) {
            sum += val.get();
        }
        
        // Filtrar diretores com pelo menos 2 filmes
        if (sum >= 2) {
            result.set(sum);
            context.write(key, result);
        }
    }
}
