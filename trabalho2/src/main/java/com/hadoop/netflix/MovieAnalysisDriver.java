package com.hadoop.netflix;

import com.hadoop.netflix.analysis.*;
import com.hadoop.netflix.recommendation.MovieRecommendationMapper;
import com.hadoop.netflix.recommendation.MovieRecommendationReducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Driver principal para análise Netflix e geração de recomendação de filme
 */
public class MovieAnalysisDriver extends Configured implements Tool {

    @Override
    public int run(String[] args) throws Exception {
        List<String> cleanArgs = new ArrayList<>();
        for (String arg : args) {
            if (arg != null && !arg.isEmpty() && !arg.contains("MovieAnalysisDriver")) {
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
        
        // Configurar para evitar problemas com staging directory no Windows
        conf.set("mapreduce.jobtracker.staging.root.dir", "file:///C:/netflix-hadoop/trabalho2/tmp/staging");
        conf.set("hadoop.tmp.dir", "file:///C:/netflix-hadoop/trabalho2/tmp");
        
        FileSystem fs = FileSystem.get(conf);

        Path inPath = new Path(inputPath);
        Path outPath = new Path(outputPath);
        Path tempPath = new Path(outputPath + "_temp");

        // Limpar diretórios com retry para Windows
        deletePathWithRetry(fs, tempPath, 3);
        deletePathWithRetry(fs, outPath, 3);

        System.out.println("╔═══════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║              TRABALHO 2 - ANÁLISE NETFLIX E RECOMENDAÇÃO                  ║");
        System.out.println("║                     Processamento via Hadoop MapReduce                    ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // Job 1: Análise de Gêneros
        if (!runGenreAnalysis(conf, inPath, new Path(tempPath, "genres"))) {
            return 1;
        }

        // Job 2: Análise de Duração
        if (!runDurationAnalysis(conf, inPath, new Path(tempPath, "duration"))) {
            return 1;
        }

        // Job 3: Análise de Elenco
        if (!runCastAnalysis(conf, inPath, new Path(tempPath, "cast"))) {
            return 1;
        }

        // Job 4: Análise de Diretores
        if (!runDirectorAnalysis(conf, inPath, new Path(tempPath, "directors"))) {
            return 1;
        }

        // Job 5: Análise de Palavras-Chave
        if (!runKeywordAnalysis(conf, inPath, new Path(tempPath, "keywords"))) {
            return 1;
        }

        // Job 6: Análise de Países
        if (!runCountryAnalysis(conf, inPath, new Path(tempPath, "countries"))) {
            return 1;
        }

        // Job 7: Geração da Recomendação Final
        System.out.println();
        System.out.println("╔═══════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                  JOB 7: GERAÇÃO DA RECOMENDAÇÃO FINAL                     ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════╝");

        Job finalJob = Job.getInstance(conf, "Recomendação Final de Filme");
        finalJob.setJarByClass(MovieAnalysisDriver.class);

        finalJob.setMapperClass(MovieRecommendationMapper.class);
        finalJob.setReducerClass(MovieRecommendationReducer.class);
        finalJob.setNumReduceTasks(1);

        finalJob.setOutputKeyClass(Text.class);
        finalJob.setOutputValueClass(Text.class);

        // Adicionar cada arquivo de resultado individualmente
        FileInputFormat.addInputPath(finalJob, new Path(tempPath, "genres/part-r-00000"));
        FileInputFormat.addInputPath(finalJob, new Path(tempPath, "duration/part-r-00000"));
        FileInputFormat.addInputPath(finalJob, new Path(tempPath, "cast/part-r-00000"));
        FileInputFormat.addInputPath(finalJob, new Path(tempPath, "directors/part-r-00000"));
        FileInputFormat.addInputPath(finalJob, new Path(tempPath, "keywords/part-r-00000"));
        FileInputFormat.addInputPath(finalJob, new Path(tempPath, "countries/part-r-00000"));
        
        FileOutputFormat.setOutputPath(finalJob, new Path(outPath, "final_recommendation"));

        System.out.println("▸ Consolidando resultados das análises...");
        boolean success = finalJob.waitForCompletion(true);

        if (!success) {
            System.err.println("✗ Erro na geração da recomendação final!");
            return 1;
        }

        System.out.println("✓ Recomendação gerada com sucesso!");
        System.out.println();
        System.out.println("╔═══════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    PROCESSAMENTO CONCLUÍDO!                               ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("📂 Resultado final: " + outPath + "/final_recommendation/part-r-00000");
        System.out.println();

        return 0;
    }

    private boolean runGenreAnalysis(Configuration conf, Path input, Path output) throws Exception {
        System.out.println("┌───────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ JOB 1: Análise de Gêneros                                                 │");
        System.out.println("└───────────────────────────────────────────────────────────────────────────┘");

        Job job = Job.getInstance(conf, "Análise de Gêneros");
        job.setJarByClass(MovieAnalysisDriver.class);

        job.setMapperClass(GenreAnalysisMapper.class);
        job.setReducerClass(GenreAnalysisReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, input);
        FileOutputFormat.setOutputPath(job, output);

        System.out.println("▸ Identificando gêneros mais populares...");
        boolean success = job.waitForCompletion(true);
        
        if (success) {
            System.out.println("✓ Análise de gêneros concluída!");
        } else {
            System.err.println("✗ Erro na análise de gêneros!");
        }
        System.out.println();
        
        return success;
    }

    private boolean runDurationAnalysis(Configuration conf, Path input, Path output) throws Exception {
        System.out.println("┌───────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ JOB 2: Análise de Duração                                                 │");
        System.out.println("└───────────────────────────────────────────────────────────────────────────┘");

        Job job = Job.getInstance(conf, "Análise de Duração");
        job.setJarByClass(MovieAnalysisDriver.class);

        job.setMapperClass(DurationAnalysisMapper.class);
        job.setReducerClass(DurationAnalysisReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, input);
        FileOutputFormat.setOutputPath(job, output);

        System.out.println("▸ Calculando estatísticas de duração de filmes...");
        boolean success = job.waitForCompletion(true);
        
        if (success) {
            System.out.println("✓ Análise de duração concluída!");
        } else {
            System.err.println("✗ Erro na análise de duração!");
        }
        System.out.println();
        
        return success;
    }

    private boolean runCastAnalysis(Configuration conf, Path input, Path output) throws Exception {
        System.out.println("┌───────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ JOB 3: Análise de Elenco                                                  │");
        System.out.println("└───────────────────────────────────────────────────────────────────────────┘");

        Job job = Job.getInstance(conf, "Análise de Elenco");
        job.setJarByClass(MovieAnalysisDriver.class);

        job.setMapperClass(CastAnalysisMapper.class);
        job.setReducerClass(CastAnalysisReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, input);
        FileOutputFormat.setOutputPath(job, output);

        System.out.println("▸ Identificando atores mais frequentes...");
        boolean success = job.waitForCompletion(true);
        
        if (success) {
            System.out.println("✓ Análise de elenco concluída!");
        } else {
            System.err.println("✗ Erro na análise de elenco!");
        }
        System.out.println();
        
        return success;
    }

    private boolean runDirectorAnalysis(Configuration conf, Path input, Path output) throws Exception {
        System.out.println("┌───────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ JOB 4: Análise de Diretores                                               │");
        System.out.println("└───────────────────────────────────────────────────────────────────────────┘");

        Job job = Job.getInstance(conf, "Análise de Diretores");
        job.setJarByClass(MovieAnalysisDriver.class);

        job.setMapperClass(DirectorAnalysisMapper.class);
        job.setReducerClass(DirectorAnalysisReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, input);
        FileOutputFormat.setOutputPath(job, output);

        System.out.println("▸ Identificando diretores mais prolíficos...");
        boolean success = job.waitForCompletion(true);
        
        if (success) {
            System.out.println("✓ Análise de diretores concluída!");
        } else {
            System.err.println("✗ Erro na análise de diretores!");
        }
        System.out.println();
        
        return success;
    }

    private boolean runKeywordAnalysis(Configuration conf, Path input, Path output) throws Exception {
        System.out.println("┌───────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ JOB 5: Análise de Palavras-Chave                                          │");
        System.out.println("└───────────────────────────────────────────────────────────────────────────┘");

        Job job = Job.getInstance(conf, "Análise de Palavras-Chave");
        job.setJarByClass(MovieAnalysisDriver.class);

        job.setMapperClass(KeywordAnalysisMapper.class);
        job.setReducerClass(KeywordAnalysisReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, input);
        FileOutputFormat.setOutputPath(job, output);

        System.out.println("▸ Extraindo palavras-chave mais comuns em descrições...");
        boolean success = job.waitForCompletion(true);
        
        if (success) {
            System.out.println("✓ Análise de palavras-chave concluída!");
        } else {
            System.err.println("✗ Erro na análise de palavras-chave!");
        }
        System.out.println();
        
        return success;
    }

    private boolean runCountryAnalysis(Configuration conf, Path input, Path output) throws Exception {
        System.out.println("┌───────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ JOB 6: Análise de Países                                                  │");
        System.out.println("└───────────────────────────────────────────────────────────────────────────┘");

        Job job = Job.getInstance(conf, "Análise de Países");
        job.setJarByClass(MovieAnalysisDriver.class);

        job.setMapperClass(CountryAnalysisMapper.class);
        job.setReducerClass(CountryAnalysisReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, input);
        FileOutputFormat.setOutputPath(job, output);

        System.out.println("▸ Identificando principais países produtores...");
        boolean success = job.waitForCompletion(true);
        
        if (success) {
            System.out.println("✓ Análise de países concluída!");
        } else {
            System.err.println("✗ Erro na análise de países!");
        }
        System.out.println();
        
        return success;
    }

    /**
     * Tenta deletar um path com retry (útil para Windows onde arquivos podem estar em uso)
     */
    private static void deletePathWithRetry(FileSystem fs, Path path, int maxRetries) throws Exception {
        for (int i = 0; i < maxRetries; i++) {
            try {
                if (fs.exists(path)) {
                    boolean deleted = fs.delete(path, true);
                    if (deleted || !fs.exists(path)) {
                        return; // Sucesso
                    }
                } else {
                    return; // Não existe, ok
                }
            } catch (Exception e) {
                if (i == maxRetries - 1) {
                    System.err.println("AVISO: Não foi possível deletar " + path + ": " + e.getMessage());
                }
            }
            
            if (i < maxRetries - 1) {
                Thread.sleep(1000); // Aguardar 1 segundo antes de tentar novamente
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new MovieAnalysisDriver(), args);
        System.exit(res);
    }
}
