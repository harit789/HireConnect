package com.hireconnect.job.repository;

import com.hireconnect.job.entity.JobDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobSearchRepository extends ElasticsearchRepository<JobDocument, String> {

    List<JobDocument> findByTitleContainingOrDescriptionContaining(String title, String description);
    List<JobDocument> findByStatus(String status);
}
