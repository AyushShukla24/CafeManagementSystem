package com.inn.cafe.POJO;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.boot.autoconfigure.web.WebProperties;

import java.io.Serializable;

@NamedQuery(name = "Category.getAllCategory", query = "select c from Category c")


@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name="Category")
public class Category implements Serializable {

    private static final long serialVersionUIS = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="name")
    private String name;

}
