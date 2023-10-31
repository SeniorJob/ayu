package com.seniorjob.seniorjobserver.service;

import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.dto.MyPageCreateLectureDto;
import com.seniorjob.seniorjobserver.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class MyPageCreateLectureService {
    private final LectureRepository lectureRepository;
    private final LectureService lectureService;
    private final UserRepository userRepository;
    private final WeekRepository weekRepository;
    private final WeekPlanRepository weekPlanRepository;
    private final AttendanceRepository attendanceRepository;
    private final CompletionLectureRepository completionLectureRepository;
    private final LectureApplyRepository lectureApplyRepository;

    @Autowired
    public MyPageCreateLectureService(WeekRepository weekRepository, WeekPlanRepository weekPlanRepository,
                                      CompletionLectureRepository completionLectureRepository, AttendanceRepository attendanceRepository,
                                      UserRepository userRepository, LectureRepository lectureRepository,
                                      LectureService lectureService, LectureApplyRepository lectureApplyRepository) {
        this.weekRepository = weekRepository;
        this.weekPlanRepository = weekPlanRepository;
        this.completionLectureRepository = completionLectureRepository;
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.lectureRepository = lectureRepository;
        this.lectureService = lectureService;
        this.lectureApplyRepository = lectureApplyRepository;
    }

    // 마이페이지(개설강좌) - 로그인된 회원이 개설한 강좌전체검색
    public List<MyPageCreateLectureDto> getMyPageCreateLectureAll(Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(()-> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
        List<LectureEntity> myLectureAll = lectureRepository.findAllByUser(user);
        return myLectureAll.stream().map(MyPageCreateLectureDto::new).collect(Collectors.toList());
    }

    // 마이페이지(개설강좌) - 강좌 상세보기
    public MyPageCreateLectureDto getLectureDetail(Long lectureId) {
        LectureEntity lectureEntity = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new NoSuchElementException("강좌 아이디를 찾을 수 없습니다. lectureId: " + lectureId));
        return new MyPageCreateLectureDto(lectureEntity);
    }


    // 마이페이지(개설강좌) - 목록 필터링

    // 강좌ID기반 강좌상태 가져오는 메서드
    public LectureEntity.LectureStatus getLectureStatus(Long create_id) {
        LectureEntity lectureEntity = lectureRepository.findById(create_id)
                .orElseThrow(() -> new RuntimeException("강좌아이디 찾지못함 create_id: " + create_id));
        return lectureEntity.getStatus();
    }

    // 강좌검색 : 제목
    public List<MyPageCreateLectureDto> searchLecturesByTitle(String title) {
        List<MyPageCreateLectureDto> myPageCreateLectureList = new ArrayList<>();
        if (title.length() >= 2) { // 2글자 이상인 경우에만 검색 수행
            List<LectureEntity> lectureEntities = lectureRepository.findByTitleContaining(title);
            myPageCreateLectureList = lectureEntities.stream()
                    .map(MyPageCreateLectureDto::new)
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("검색어는 \"2글자\" 이상 입력해주세요!");
        }
        // 검색 결과가 없는 경우
        if (myPageCreateLectureList.isEmpty()) {
            throw new NoSuchElementException("검색결과에 해당하는 강좌가 없습니다.ㅠㅠ");
        }
        return myPageCreateLectureList;
    }

    // 강좌정렬
    // 최신순으로 강좌 정렬 최신 = true 오래된 = false
    public List<MyPageCreateLectureDto> sortLecturesByCreatedDate(List<MyPageCreateLectureDto> lectureList, boolean descending) {
        lectureList.sort((a, b) -> descending ?
                b.getCreatedDate().compareTo(a.getCreatedDate()) :
                a.getCreatedDate().compareTo(b.getCreatedDate()));
        return lectureList;
    }

    // 기존코드 인기순 : max_participant가많은순 -> 강좌 참여하기를 만들때 실제참여자가 많은순으로 변경할것임
    // 수정된 인기순 : 강좌에 참여한 사람이 많은 강좌순 : 참여자수 current_participants
    public List<MyPageCreateLectureDto> sortLecturesByPopularity(List<MyPageCreateLectureDto> lectureList, boolean descending) {
        lectureList.sort((a, b) -> descending ?
                b.getCurrent_participants() - a.getCurrent_participants() :
                a.getCurrent_participants() - b.getCurrent_participants());
        return lectureList;
    }

    // 가격순 : prices(낮은순 높은순)
    public List<MyPageCreateLectureDto> sortLecturesByPrice(List<MyPageCreateLectureDto> lectureList, boolean descending) {
        lectureList.sort((a, b) -> descending ?
                b.getPrice().compareTo(a.getPrice()) :
                a.getPrice().compareTo(b.getPrice()));
        return lectureList;
    }

    // 강좌상태검색
    public List<MyPageCreateLectureDto> filterStatus(List<MyPageCreateLectureDto> lectureList, LectureEntity.LectureStatus status) {
        return lectureList.stream()
                .filter(lecture -> lecture.getStatus() == status)
                .collect(Collectors.toList());
    }

    // 필터링 : 제목검색 -> 최신순,오래된순, 가격높은순, 가격낮은순, 인기순, 지역(시,군),
    // 상좌상태(모집중 = 신청가능상태,  개설대기중 = 개설대기상태, 진행중 = 진행상태), 카테고리
    public List<MyPageCreateLectureDto> filterLectures(List<MyPageCreateLectureDto> lectureList, String filter, boolean descending) {
        switch (filter){
            case "latest":
                return sortLecturesByCreatedDate(lectureList, descending);
            case "price":
                return sortLecturesByPrice(lectureList, descending);
            case  "popularity":
                return sortLecturesByPopularity(lectureList, descending);
            default:
                throw new IllegalArgumentException("잘못된 필터조건");
        }
    }

    //페이징
    public Page<LectureEntity> getLectures(Pageable pageable) {
        return lectureRepository.findAll(pageable);
    }
}
