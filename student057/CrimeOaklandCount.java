// ======================= WordCount.java ==========================================
package org.myorg;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.*;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;


public class CrimeOaklandCount extends Configured implements Tool {

        public static class CrimeCountMap extends Mapper<LongWritable, Text, Text, IntWritable>
        {
                private final static IntWritable one = new IntWritable(1);
                private final static double xForbes= 1354326.897;
                private final static double yForbes= 411447.7828;
                private Text crime = new Text();
                
                @Override
                public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
                {
                        String line = value.toString();
                        StringTokenizer tokenizer = new StringTokenizer(line);
                        while(tokenizer.hasMoreTokens())
                        {
                                if(str.contains("Offense"))
                                        continue;
                                String str= tokenizer.nextToken("\n");
                                String[] arrStr= str.split("\t");
                                if(arrStr.length==7){
                                        double xlabel= Double.parseDouble(arrStr[0]);
                                        double ylabel= Double.parseDouble(arrStr[1]);
                                        double dist= Math.sqrt(Math.pow((xlabel-xForbes),2)+Math.pow((ylabel-yForbes),2));
                                        if(dist<2000){
                                                context.write("near forbes", one);
                                        }
                                }
                        }
                }
        }
        
        public static class CrimeCountReducer extends Reducer<Text, IntWritable, Text, IntWritable>
        {
                public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
                {
                        int sum = 0;
                        for(IntWritable value: values)
                        {
                                sum += value.get();
                        }
                        context.write(key, new IntWritable(sum));
                }
                
        }
        
        public int run(String[] args) throws Exception  {
               
                Job job = new Job(getConf());
                job.setJarByClass(CrimeOaklandCount.class);
                job.setJobName("crimecount");
                
                job.setOutputKeyClass(Text.class);
                job.setOutputValueClass(IntWritable.class);
                
                job.setMapperClass(CrimeCountMap.class);
                job.setCombinerClass(CrimeCountReducer.class);
                job.setReducerClass(CrimeCountReducer.class);
                
                
                job.setInputFormatClass(TextInputFormat.class);
                job.setOutputFormatClass(TextOutputFormat.class);
                
                
                FileInputFormat.setInputPaths(job, new Path(args[0]));
                FileOutputFormat.setOutputPath(job, new Path(args[1]));
                
                boolean success = job.waitForCompletion(true);
                return success ? 0: 1;
        }
        
       
        public static void main(String[] args) throws Exception {
                // TODO Auto-generated method stub
                int result = ToolRunner.run(new CrimeOaklandCount(), args);
                System.exit(result);
        }
       
} 
