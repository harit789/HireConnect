package com.hireconnect.user.service;

import java.util.List;

import com.hireconnect.user.entity.CandidateProfile;
import com.hireconnect.user.entity.RecruiterProfile;

public interface ProfileService {

    CandidateProfile addCandidateProfile(CandidateProfile profile);

    RecruiterProfile addRecruiterProfile(RecruiterProfile profile);

    CandidateProfile getCandidateByUserId(Long userId);

    RecruiterProfile getRecruiterByUserId(Long userId);

    CandidateProfile getCandidateById(Long profileId);

    RecruiterProfile getRecruiterById(Long profileId);

    CandidateProfile updateCandidateProfile(Long userId, CandidateProfile updated);

    RecruiterProfile updateRecruiterProfile(Long userId, RecruiterProfile updated);

    CandidateProfile bookmarkJob(Long userId, Long jobId);

    CandidateProfile removeBookmark(Long userId, Long jobId);

    CandidateProfile uploadResume(Long userId, org.springframework.web.multipart.MultipartFile file);

    void deleteCandidateProfile(Long userId);

    void deleteRecruiterProfile(Long userId);

    List<CandidateProfile> getAllCandidates();

    List<RecruiterProfile> getAllRecruiters();

    RecruiterProfile joinTeam(Long userId, Long teamId);
}
