package com.shambhu.SpringBatch.config;

import com.shambhu.SpringBatch.dto.Customer;
import com.shambhu.SpringBatch.repo.CustomerRepo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@AllArgsConstructor
public class SpringBatchConfig {

    private CustomerRepo customerRepo;

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
    public RepositoryItemWriter<Customer> writer(){
        RepositoryItemWriter<Customer> itemWriter=new RepositoryItemWriter<>();
        itemWriter.setRepository(customerRepo);
        itemWriter.setMethodName("save");
        return itemWriter;
    }
    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
         return new StepBuilder("scv-step",jobRepository)
                 .<Customer,Customer>chunk(10,platformTransactionManager)
                 .reader(flatFileItemReader())
                 .processor(customerProcessor())
                 .writer(writer())
                 .taskExecutor(taskExecutor())
                 .build();
    }

    @Bean
    public Job importCustomerJob(JobRepository jobRepository,Step step1){
        return new JobBuilder("importCustomerJob",jobRepository)
                .start(step1)
                .build();
    }
    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(10);
        return asyncTaskExecutor;
    }

}
