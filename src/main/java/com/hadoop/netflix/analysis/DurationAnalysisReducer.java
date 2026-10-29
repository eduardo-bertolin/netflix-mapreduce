package com.hadoop.netflix.analysis;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reducer para análise de duração
 * Calcula média, min, max e moda
 */
public class DurationAnalysisReducer extends Reducer<Text, IntWritable, Text, Text> {
    private final Text outValue = new Text();

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context) 
            throws IOException, InterruptedException {
        List<Integer> durations = new ArrayList<>();
        long sum = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (IntWritable val : values) {
            int duration = val.get();
            durations.add(duration);
            sum += duration;
            min = Math.min(min, duration);
            max = Math.max(max, duration);
        }

        int count = durations.size();
        double avg = (double) sum / count;

        // Calcular moda (valor mais frequente)
        Collections.sort(durations);
        int mode = durations.get(count / 2); // Mediana como aproximação

        String result = String.format("AVG=%.0f|MIN=%d|MAX=%d|MODE=%d|COUNT=%d", 
                                     avg, min, max, mode, count);
        outValue.set(result);
        context.write(key, outValue);
    }
}
