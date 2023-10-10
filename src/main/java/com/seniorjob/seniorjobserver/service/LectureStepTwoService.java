package com.seniorjob.seniorjobserver.service;

import com.seniorjob.seniorjobserver.repository.*;
import com.seniorjob.seniorjobserver.repository.CompletionLectureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LectureStepTwoService {
    // 강좌개설 2단계API 구현
    private final WeekRepository weekRepository;
    private final WeekPlanRepository weekPlanRepository;
    private final CompletionLectureRepository completionLectureRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;

    @Autowired
    public LectureStepTwoService(WeekRepository weekRepository, WeekPlanRepository weekPlanRepository,
                                 CompletionLectureRepository completionLectureRepository, AttendanceRepository attendanceRepository,
                                 UserRepository userRepository, LectureRepository lectureRepository) {
        this.weekRepository = weekRepository;
        this.weekPlanRepository = weekPlanRepository;
        this.completionLectureRepository = completionLectureRepository;
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.lectureRepository = lectureRepository;
    }

}
