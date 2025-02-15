package com.shambhu.SpringBatch.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "customer_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class Customer {
    @Id
    @Column(name = "customer_id")
    public int id;
    @Column(name = "first_name")
    public String firstName;
    @Column(name = "last_name")
    public String lastName;
    public String email;
    public String gender;
    @Column(name = "contact_no")
    public String contactNumber;
    public String country;
    @Column(name = "DOB")
    public String dob;

}
