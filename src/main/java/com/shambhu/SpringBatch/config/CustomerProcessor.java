package com.shambhu.SpringBatch.config;

import com.shambhu.SpringBatch.dto.Customer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerProcessor implements ItemProcessor<Customer,Customer> {
    @Override
    public Customer process(Customer customer) throws Exception { //It filters the data
        /*if (customer.getCountry().equals("United States")) {*/
            return customer;
       /* }else {
            return null;
        }*/
    }
}
