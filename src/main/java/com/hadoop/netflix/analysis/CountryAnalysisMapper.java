package com.hadoop.netflix.analysis;

import com.hadoop.netflix.CsvParser;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.List;

/**
 * Mapper para análise de países de origem
 */
public class CountryAnalysisMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    private final Text countryKey = new Text();
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
        String country = fields.get(5).replace("\"", "").trim();

        // Filtrar apenas filmes
        if (!type.equalsIgnoreCase("Movie")) {
            return;
        }

        // Processar primeiro país (geralmente o principal)
        if (!country.isEmpty()) {
            String[] countries = country.split(",");
            String primaryCountry = countries[0].trim();
            if (!primaryCountry.isEmpty()) {
                countryKey.set(primaryCountry);
                context.write(countryKey, one);
            }
        }
    }
}
