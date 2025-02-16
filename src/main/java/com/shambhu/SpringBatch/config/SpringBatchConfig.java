package com.shambhu.SpringBatch.config;

import com.shambhu.SpringBatch.dto.Customer;
import com.shambhu.SpringBatch.partitioner.ColumnRangePartitioner;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@AllArgsConstructor
public class SpringBatchConfig {

    private CustomerWriter customerWriter;


    @Bean
    public FlatFileItemReader<Customer> flatFileItemReader(){
       FlatFileItemReader<Customer> reader=new FlatFileItemReader<>();
       reader.setResource(new FileSystemResource("src/main/resources/customers.csv"));
       reader.setName("csvReader");
       reader.setLinesToSkip(1);
       reader.setLineMapper(lineMapper());
       return reader;
    }

    private LineMapper<Customer> lineMapper() {
        DefaultLineMapper<Customer> defaultLineMapper=new DefaultLineMapper<>();
        DelimitedLineTokenizer delimitedLineTokenizer=new DelimitedLineTokenizer();
        delimitedLineTokenizer.setDelimiter(",");
        delimitedLineTokenizer.setStrict(false);
        delimitedLineTokenizer.setNames("id","firstName","lastName","email","gender","contactNo","country","dob");

        BeanWrapperFieldSetMapper<Customer> fieldSetMapper=new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);

        defaultLineMapper.setLineTokenizer(delimitedLineTokenizer);
        defaultLineMapper.setFieldSetMapper(fieldSetMapper);
        return defaultLineMapper;
    }

    @Bean
    public CustomerProcessor customerProcessor(){
        return new CustomerProcessor();
    }

    @Bean
    public Step slaveStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
         return new StepBuilder("slaveStep",jobRepository)
                 .<Customer,Customer>chunk(250,platformTransactionManager)
                 .reader(flatFileItemReader())
                 .processor(customerProcessor())
                 .writer(customerWriter)
                 .build();
    }
   /* @Bean
    public RepositoryItemWriter<Customer> writer(){
        RepositoryItemWriter<Customer> itemWriter=new RepositoryItemWriter<>();
        itemWriter.setRepository(customerRepo);
        itemWriter.setMethodName("save");
        return itemWriter;
    }*/
    @Bean
    public Step masterStep(JobRepository jobRepository,PlatformTransactionManager platformTransactionManager){
        return new StepBuilder("masterStep",jobRepository)
                .partitioner("slaveStep",partitioner())
                .partitionHandler(partitionHandler(jobRepository, platformTransactionManager))
                .build();
    }
    @Bean
    public ColumnRangePartitioner partitioner(){
        return new ColumnRangePartitioner();
    }
    @Bean
    public PartitionHandler partitionHandler(JobRepository jobRepository,PlatformTransactionManager platformTransactionManager){
        TaskExecutorPartitionHandler taskExecutorPartitionHandler=new TaskExecutorPartitionHandler();
        taskExecutorPartitionHandler.setGridSize(4);
        taskExecutorPartitionHandler.setTaskExecutor(taskExecutor());
        taskExecutorPartitionHandler.setStep(slaveStep(jobRepository,platformTransactionManager));
        return taskExecutorPartitionHandler;
    }

    @Bean
    public Job importCustomerJob(JobRepository jobRepository,Step masterStep){
        return new JobBuilder("importCustomerJob",jobRepository)
                .start(masterStep)
                .build();
    }
    @Bean
    public TaskExecutor taskExecutor() {
        /*SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(10);
        return asyncTaskExecutor;*/
        ThreadPoolTaskExecutor threadPoolTaskExecutor=new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setMaxPoolSize(4);
        threadPoolTaskExecutor.setCorePoolSize(4);
        threadPoolTaskExecutor.setQueueCapacity(4);
        return threadPoolTaskExecutor;
    }

}
