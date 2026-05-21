package com.hireconnect.job.repository;

import com.hireconnect.job.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByStatus(String status);

    List<Job> findByPostedBy(Long recruiterId);

    long countByPostedBy(Long recruiterId);

    List<Job> findByPostedByAndStatus(Long recruiterId, String status);

    List<Job> findByCategory(String category);

    List<Job> findByLocation(String location);

    List<Job> findByType(String type);

    List<Job> findByTitleContainingIgnoreCase(String keyword);

    List<Job> findBySalaryMinGreaterThanEqualAndSalaryMaxLessThanEqual(
            Double salaryMin, Double salaryMax);

    List<Job> findByExperienceRequiredLessThanEqual(Integer experience);

    @Query("SELECT j FROM Job j WHERE " +
           "(:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
           "   OR LOWER(j.description) LIKE LOWER(CONCAT('%',:keyword,'%'))) " +
           "AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%',:location,'%'))) " +
           "AND (:category IS NULL OR j.category = :category) " +
           "AND (:type IS NULL OR j.type = :type) " +
           "AND (:salaryMin IS NULL OR j.salaryMin >= :salaryMin) " +
           "AND (:salaryMax IS NULL OR j.salaryMax <= :salaryMax) " +
           "AND (:experience IS NULL OR j.experienceRequired >= :experience) " +
           "AND j.status = 'ACTIVE'")
    List<Job> searchJobs(
            @Param("keyword")    String keyword,
            @Param("location")   String location,
            @Param("category")   String category,
            @Param("type")       String type,
            @Param("salaryMin")  Double salaryMin,
            @Param("salaryMax")  Double salaryMax,
            @Param("experience") Integer experience
    );
}
