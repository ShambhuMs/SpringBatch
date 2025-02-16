package com.shambhu.SpringBatch.config;

import com.shambhu.SpringBatch.dto.Customer;
import com.shambhu.SpringBatch.repo.CustomerRepo;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;



@Configuration
public class CustomerWriter implements ItemWriter<Customer> {
   @Autowired
    private CustomerRepo customerRepo;
    @Override
    public void write(Chunk<? extends Customer> list) throws Exception {
        System.out.println("Thread name: "+Thread.currentThread().getName());
        customerRepo.saveAll(list.getItems());
    }
}
