package com.optimagrowth.license.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
@ToString
@Entity
@Table(name="licenses")
public class License extends RepresentationModel<License> {

    @Id
    @Column(name = "license_id", nullable = false)
    private String licenseId;

    @Column(name="description")
    private String description;

    @Column(name = "organization_id", nullable = false)
    private String organizationId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "license_type", nullable = false)
    private String licenseType;

    @Column(name="comment")
    private String comment;

    public License withComment(String comment){
        this.setComment(comment);
        return this;
    }
}

/*
* The class was changed because the focus shifted from returning hypermedia API responses to storing and retrieving licenses from a database.
* In a real-world microservice, both versions could exist — one for persistence and another for API representation.
* */

/*@Getter
@Setter
@ToString
public class License extends RepresentationModel<License> {

    private int id;

    private String licenseId;

    private String description;

    private String organizationId;

    private String productName;

    private String licenseType;
}*/