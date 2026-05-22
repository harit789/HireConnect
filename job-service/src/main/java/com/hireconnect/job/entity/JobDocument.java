package com.hireconnect.job.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "jobs")
public class JobDocument {

    @Id
    private String id;

    private Long jobId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Text)
    private String location;

    private Double salaryMin;
    private Double salaryMax;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private List<String> skills;

    private Integer experienceRequired;

    private Long postedBy;

    @Field(type = FieldType.Keyword)
    private String status;
}
