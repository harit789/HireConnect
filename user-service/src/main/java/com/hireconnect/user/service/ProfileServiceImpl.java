package com.hireconnect.user.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hireconnect.user.entity.CandidateProfile;
import com.hireconnect.user.entity.RecruiterProfile;
import com.hireconnect.user.repository.CandidateProfileRepository;
import com.hireconnect.user.repository.RecruiterProfileRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final CandidateProfileRepository candidateRepo;
    private final RecruiterProfileRepository recruiterRepo;

    @Override
    @Transactional
    public CandidateProfile addCandidateProfile(CandidateProfile profile) {
        return candidateRepo.save(profile);
    }

    @Override
    @Transactional
    public RecruiterProfile addRecruiterProfile(RecruiterProfile profile) {
        if (profile.getTeamId() == null) {
            profile.setTeamId(profile.getUserId());
        }
        return recruiterRepo.save(profile);
    }

    @Override
    @Cacheable(value = "candidateByUserId", key = "#userId")
    public CandidateProfile getCandidateByUserId(Long userId) {
        return candidateRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Candidate profile not found for userId: " + userId));
    }

    @Override
    @Cacheable(value = "recruiterByUserId", key = "#userId")
    public RecruiterProfile getRecruiterByUserId(Long userId) {
        return recruiterRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Recruiter profile not found for userId: " + userId));
    }

    @Override
    public CandidateProfile getCandidateById(Long profileId) {
        return candidateRepo.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Candidate profile not found: " + profileId));
    }

    @Override
    public RecruiterProfile getRecruiterById(Long profileId) {
        return recruiterRepo.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Recruiter profile not found: " + profileId));
    }

    @Override
    @Transactional
    @CachePut(value = "candidateByUserId", key = "#userId")
    public CandidateProfile updateCandidateProfile(Long userId, CandidateProfile updated) {
        CandidateProfile existing = getCandidateByUserId(userId);
        updated.setProfileId(existing.getProfileId());
        updated.setUserId(userId);
        return candidateRepo.save(updated);
    }

    @Override
    @Transactional
    @CachePut(value = "recruiterByUserId", key = "#userId")
    public RecruiterProfile updateRecruiterProfile(Long userId, RecruiterProfile updated) {
        RecruiterProfile existing = getRecruiterByUserId(userId);
        updated.setProfileId(existing.getProfileId());
        updated.setUserId(userId);
        return recruiterRepo.save(updated);
    }

    @Override
    @Transactional
    public CandidateProfile bookmarkJob(Long userId, Long jobId) {
        CandidateProfile profile = getCandidateByUserId(userId);
        List<Long> saved = profile.getSavedJobs();
        if (saved == null) {
            saved = new java.util.ArrayList<>();
            profile.setSavedJobs(saved);
        }
        if (!saved.contains(jobId)) {
            saved.add(jobId);
        }
        return candidateRepo.save(profile);
    }

    @Override
    @Transactional
    public CandidateProfile removeBookmark(Long userId, Long jobId) {
        CandidateProfile profile = getCandidateByUserId(userId);
        List<Long> saved = profile.getSavedJobs();
        if (saved != null && saved.contains(jobId)) {
            saved.remove(jobId);
            return candidateRepo.save(profile);
        }
        return profile;
    }

    @Override
    @Transactional
    public CandidateProfile uploadResume(Long userId, org.springframework.web.multipart.MultipartFile file) {
        CandidateProfile profile = getCandidateByUserId(userId);
        
        try {

            String uploadDir = "f:/HireConnect-Resume/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            String fileName = userId + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            String resumeUrl = "/uploads/" + fileName;
            profile.setResumeUrl(resumeUrl);
            

            PDDocument document = PDDocument.load(filePath.toFile());
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document).toLowerCase();
            document.close();
            
            java.util.List<String> skills = profile.getSkills() != null ? new java.util.ArrayList<>(profile.getSkills()) : new java.util.ArrayList<>();
            

            String[] knownSkills = {"java", "spring boot", "python", "react", "angular", "mysql", "docker", "aws"};
            for (String skill : knownSkills) {
                if (text.contains(skill) && !skills.contains(skill)) {
                    skills.add(skill);
                }
            }
            profile.setSkills(skills);
            

            int parsedExp = 0;
            if (text.contains("5+ years") || text.contains("5 years")) parsedExp = 5;
            else if (text.contains("3+ years") || text.contains("3 years")) parsedExp = 3;
            else if (text.contains("2+ years") || text.contains("2 years")) parsedExp = 2;
            else if (text.contains("1 year") || text.contains("1+ years")) parsedExp = 1;
            
            if (parsedExp > 0 || profile.getExperience() == null) {
                profile.setExperience(parsedExp > 0 ? parsedExp : 0);
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload and parse resume", e);
        }
        
        return candidateRepo.save(profile);
    }

    @Override
    @Transactional
    @CacheEvict(value = "candidateByUserId", key = "#userId")
    public void deleteCandidateProfile(Long userId) {
        candidateRepo.findByUserId(userId)
                .ifPresent(p -> candidateRepo.deleteById(p.getProfileId()));
    }

    @Override
    @Transactional
    @CacheEvict(value = "recruiterByUserId", key = "#userId")
    public void deleteRecruiterProfile(Long userId) {
        recruiterRepo.findByUserId(userId)
                .ifPresent(p -> recruiterRepo.deleteById(p.getProfileId()));
    }

    @Override
    public List<CandidateProfile> getAllCandidates() {
        return candidateRepo.findAll();
    }

    @Override
    public List<RecruiterProfile> getAllRecruiters() {
        return recruiterRepo.findAll();
    }

    @Override
    @Transactional
    public RecruiterProfile joinTeam(Long userId, Long teamId) {
        RecruiterProfile profile = getRecruiterByUserId(userId);
        profile.setTeamId(teamId);
        return recruiterRepo.save(profile);
    }
}
