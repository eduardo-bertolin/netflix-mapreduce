package com.hadoop.netflix;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class NetflixDriver extends Configured implements Tool {

    @Override
    public int run(String[] args) throws Exception {
        List<String> cleanArgs = new ArrayList<>();
        for (String arg : args) {
            if (arg != null && !arg.isEmpty() && !arg.contains("NetflixDriver")) {
                cleanArgs.add(arg);
            }
        }

        String inputPath = "input/netflix_titles.csv";
        String outputPath = "output";

        if (cleanArgs.size() >= 2) {
            inputPath = cleanArgs.get(0);
            outputPath = cleanArgs.get(1);
        } else if (cleanArgs.size() == 1) {
            inputPath = cleanArgs.get(0);
        }

        Configuration conf = getConf();
        FileSystem fs = FileSystem.get(conf);

        Path inPath = new Path(inputPath);
        Path outPath = new Path(outputPath);
        Path tempPath = new Path(outputPath + "_temp");

        if (fs.exists(tempPath)) {
            fs.delete(tempPath, true);
        }
        if (fs.exists(outPath)) {
            fs.delete(outPath, true);
        }

        System.out.println("================================================================================");
        System.out.println("                   INICIANDO JOB 1: MAPREDUCE                               ");
        System.out.println("                   Contagem de Palavras e Métricas                            ");
        System.out.println("================================================================================");
        System.out.println(" • Diretório de Entrada (Input) : " + inPath);
        System.out.println(" • Diretório Temporário (Temp)  : " + tempPath);
        System.out.println("──────────────────────────────────────────────────");

        Job job1 = Job.getInstance(conf, "Netflix WordCount and Stats");
        job1.setJarByClass(NetflixDriver.class);

        job1.setMapperClass(NetflixStatsMapper.class);
        job1.setReducerClass(NetflixStatsReducer.class);

        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job1, inPath);
        FileOutputFormat.setOutputPath(job1, tempPath);

        boolean job1Success = job1.waitForCompletion(true);
        if (!job1Success) {
            System.err.println("Erro na execucao do Job 1!");
            return 1;
        }

        System.out.println("================================================================================");
        System.out.println("                   INICIANDO JOB 2: MAPREDUCE                               ");
        System.out.println("                   Cálculo de Top/Bottom e Relatório                            ");
        System.out.println("================================================================================");
        System.out.println(" • Diretório Temporário (Input) : " + tempPath);
        System.out.println(" • Diretório Final (Output)     : " + outPath);
        System.out.println("──────────────────────────────────────────────────");

        Job job2 = Job.getInstance(conf, "Netflix Top/Bottom Summary");
        job2.setJarByClass(NetflixDriver.class);

        job2.setMapperClass(NetflixSummaryMapper.class);
        job2.setReducerClass(NetflixSummaryReducer.class);

        job2.setNumReduceTasks(1);

        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job2, tempPath);
        FileOutputFormat.setOutputPath(job2, outPath);

        boolean job2Success = job2.waitForCompletion(true);
        if (!job2Success) {
            System.err.println("Erro na execucao do Job 2!");
            return 1;
        }

        System.out.println("================================================================================");
        System.out.println("                   PROCESSO CONCLUÍDO COM SUCESSO!                            ");
        System.out.println("================================================================================");
        System.out.println(" • Relatório Final           : " + outPath);
        System.out.println(" • Estatísticas Temporárias    : " + tempPath);

        return 0;
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new NetflixDriver(), args);
        System.exit(res);
    }
}