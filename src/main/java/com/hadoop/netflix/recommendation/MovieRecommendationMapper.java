package com.hadoop.netflix.recommendation;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;

/**
 * Mapper para consolidação de resultados das análises
 */
public class MovieRecommendationMapper extends Mapper<LongWritable, Text, Text, Text> {
    private final Text outKey = new Text();
    private final Text outValue = new Text();

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString().trim();
        if (line.isEmpty()) {
            return;
        }

        // Identificar de qual job veio o resultado
        FileSplit fileSplit = (FileSplit) context.getInputSplit();
        String filename = fileSplit.getPath().getParent().getName();

        // Emitir com prefixo identificando a origem
        outKey.set(filename);
        outValue.set(line);
        context.write(outKey, outValue);
    }
}
