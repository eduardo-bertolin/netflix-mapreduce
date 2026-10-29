package com.hadoop.netflix;

import com.hadoop.netflix.recommendation.MovieRecommendationMapper;
import com.hadoop.netflix.recommendation.MovieRecommendationReducer;
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

/**
 * Driver simplificado que executa apenas o Job 7 (Recomendação Final)
 * usando os resultados já gerados pelos jobs anteriores
 */
public class FinalRecommendationDriver extends Configured implements Tool {

    @Override
    public int run(String[] args) throws Exception {
        String tempPath = "output_temp";
        String outputPath = "output";

        Configuration conf = getConf();
        
        // Configurar para evitar problemas com staging directory
        conf.set("mapreduce.jobtracker.staging.root.dir", "file:///C:/netflix-hadoop/tmp/staging");
        conf.set("hadoop.tmp.dir", "file:///C:/netflix-hadoop/tmp");
        
        FileSystem fs = FileSystem.get(conf);

        Path outPath = new Path(outputPath);

        // Limpar apenas o diretório de output final
        if (fs.exists(outPath)) {
            fs.delete(outPath, true);
        }

        System.out.println("╔═══════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                  JOB 7: GERAÇÃO DA RECOMENDAÇÃO FINAL                     ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        Job finalJob = Job.getInstance(conf, "Recomendação Final de Filme");
        finalJob.setJarByClass(FinalRecommendationDriver.class);

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
        System.out.println("📂 Resultado final: " + outputPath + "/final_recommendation/part-r-00000");
        System.out.println();

        return 0;
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new FinalRecommendationDriver(), args);
        System.exit(res);
    }
}
