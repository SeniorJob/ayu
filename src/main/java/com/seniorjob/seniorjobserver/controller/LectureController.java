package com.seniorjob.seniorjobserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.seniorjob.seniorjobserver.dto.CompleteLectureDataDto;
import com.seniorjob.seniorjobserver.repository.LectureRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.dto.LectureDto;
import com.seniorjob.seniorjobserver.repository.UserRepository;
import com.seniorjob.seniorjobserver.service.LectureService;
import com.seniorjob.seniorjobserver.service.StorageService;
import com.seniorjob.seniorjobserver.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/lectures")
public class LectureController {
	private final LectureService lectureService;
	private final StorageService storageService;
	private final UserRepository userRepository;
	private final LectureRepository lectureRepository;
	private final UserService userService;
	private static final Logger log = LoggerFactory.getLogger(LectureController.class);

	public LectureController(LectureService lectureService, StorageService storageService, UserRepository userRepository, UserService userService, LectureRepository lectureRepository) {
		this.lectureService = lectureService;
		this.storageService = storageService;
		this.userRepository = userRepository;
		this.userService = userService;
		this.lectureRepository = lectureRepository;
	}

	// 강좌개설API 1단계
	// POST /api/lectures
	@PreAuthorize("isAuthenticated()")
	@PostMapping
	public ResponseEntity<LectureDto> createLecture(
			@RequestParam("file") MultipartFile file,
			@RequestParam("lectureDto") String lectureDtoJson,
			@AuthenticationPrincipal UserDetails userDetails
	) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		LectureDto lectureDto = objectMapper.readValue(lectureDtoJson, LectureDto.class);

		UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

		lectureDto.setUser(currentUser); // 현재 로그인된 사용자의 정보를 강좌 DTO에 설정
		lectureDto.setCreator(currentUser.getName()); // 로그인된 사용자의 이름을 creator로 지정


		// 이미지 업로드
		String imageUrl = storageService.uploadImage(file);
		lectureDto.setImage_url(imageUrl);

		LectureDto createdLecture = lectureService.createLecture(lectureDto, currentUser);

		if (createdLecture.getUser() != null) {
			log.info("User assigned to createdLecture DTO: {}", createdLecture.getUser().toString());
		} else {
			log.warn("User is not assigned to createdLecture DTO");
		}

		return ResponseEntity.ok(createdLecture);
	}

	// 강좌개설 3단계 1,2단계 완료처리API
	@PostMapping("/completeCreation")
	public ResponseEntity<?> completeLectureCreation(@RequestBody CompleteLectureDataDto completeData,
													 @AuthenticationPrincipal UserDetails userDetails) {
		try {
			UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
					.orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

			completeData.setUserId(currentUser.getUid());

			lectureService.createLecture(completeData);
			return ResponseEntity.ok("강좌가 성공적으로 개설되었습니다.");
		} catch (DataIntegrityViolationException e) {
			log.error("Data integrity violation: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("데이터 유효성 오류가 발생했습니다. 입력 값을 확인해주세요.");
		} catch (Exception e) {
			log.error("Error: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("강좌 개설에 실패했습니다. 에러: " + e.getMessage());
		}
	}

	// 로그인된 유저의 개설된강좌수정API
	// PUT /api/lectures/{id}
	@PutMapping("/{id}")
	public ResponseEntity<LectureDto> updateLecture(
			@PathVariable("id") Long id,
			@RequestParam(value = "file", required = false) MultipartFile file,
			@RequestParam("lectureDto") String lectureDtoJson,
			@AuthenticationPrincipal UserDetails userDetails
	) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		LectureDto lectureDto = objectMapper.readValue(lectureDtoJson, LectureDto.class);

		UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

		// 현재 사용자가 강좌의 생성자인지 확인
		LectureEntity lectureEntity = lectureRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("강좌아이디 찾지못함 create_id: " + id));
		if (!lectureEntity.getUser().equals(currentUser)) {
			throw new RuntimeException("해당 강좌를 수정할 권한이 없습니다.");
		}

		// 새 파일이 제공된 경우
		if (file != null) {
			if (lectureEntity.getImage_url() != null && !lectureEntity.getImage_url().isEmpty()) {
				storageService.deleteImage(lectureEntity.getImage_url());  // 기존 이미지 삭제
			}
			String imageUrl = storageService.uploadImage(file);
			lectureDto.setImage_url(imageUrl);
		}

		LectureDto updatedLecture = lectureService.updateLecture(id, lectureDto);
		return ResponseEntity.ok(updatedLecture);
	}

	// 강좌전체조회API
	// GET /api/lectures
	@GetMapping("/all")
	public ResponseEntity<Object> getAllLectures() {
		List<LectureDto> lectureList = lectureService.getAllLectures();

		if (lectureList.isEmpty()) {
			return new ResponseEntity<>("현재 강좌가 존재하지 않습니다!ㅠㅠ", HttpStatus.NOT_FOUND);
		}

		for (LectureDto lectureDto : lectureList) {
			LectureEntity.LectureStatus status;
			try {
				status = lectureService.getLectureStatus(lectureDto.getCreate_id());
			} catch (IllegalArgumentException e) {
				log.error("Invalid LectureStatus value: ", e);
				continue;
			}
			lectureDto.setStatus(status);
		}
		return ResponseEntity.ok(lectureList);
	}

	// 개설된강좌삭제API
	// Delete /api/lectures/{id}
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteLecture(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetails userDetails) {

		UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
						.orElseThrow(()-> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

		LectureEntity lectureEntity = lectureRepository.findById(id)
						.orElseThrow(()-> new RuntimeException("강좌 아이디를 찾지 못함 : " + id));

		if(!lectureEntity.getUser().equals(currentUser)){
			throw new RuntimeException("해당 강좌를 삭제할 권한이 없습니다.");
		}

		lectureService.deleteLecture(id);
		String successMessage = currentUser.getName() + "님의 " + id + "번 강좌가 정상적으로 삭제되었습니다!";
		return ResponseEntity.ok(successMessage);
	}

	// 개설된강좌상세정보API
	// GET /api/lectures/detail/{id}
	@GetMapping("/detail/{id}")
	public ResponseEntity<LectureDto> getDetailLectureById(@PathVariable("id") Long id) {
		LectureDto lecture = lectureService.getDetailLectureById(id);
		if (lecture != null) {
			LectureEntity.LectureStatus status = lectureService.getLectureStatus(id);
			lecture.setStatus(status);
			return ResponseEntity.ok(lecture);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	// 세션로그인후 자신이 개설한 강좌목록 전체조회API - 회원으로 이동
	@GetMapping("/myLectureAll")
	public ResponseEntity<?> getMyLectureAll(@AuthenticationPrincipal UserDetails userDetails) {
		UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
		List<LectureDto> myLectureAll = lectureService.getMyLectureAll(currentUser.getUid());

		if (myLectureAll.isEmpty()) {
			return ResponseEntity.ok("개설된 강좌가 없습니다.");
		}

		return ResponseEntity.ok(myLectureAll);
	}

	// 세션로그인후 자신이 개설한 강좌 상세보기API- 회원으로 이동
	@GetMapping("/myLectureDetail/{id}")
	public ResponseEntity<LectureDto> getMyLectureDetail(
			@PathVariable("id") Long id,
			@AuthenticationPrincipal UserDetails userDetails
	) {
		UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

		LectureDto lecture = lectureService.getMyLectureDetail(id, currentUser.getUid());

		if (lecture != null) {
			LectureEntity.LectureStatus status = lectureService.getLectureStatus(id);
			lecture.setStatus(status);
			return ResponseEntity.ok(lecture);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	// 필터링
	// api/lectures/filter == 모든강좌조회
	// api/lectures/filter?title="강좌제목" == 제목만으로 검색
	// api/lectures/filter?title="강좌제목"&filter=최신순
	@GetMapping("/filter")
	public ResponseEntity<Page<LectureDto>> filterAndPaginateLectures(
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "filter", required = false) String filter,
			@RequestParam(value = "region", required = false) String region,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "category", required = false) String category,
			@RequestParam(defaultValue = "0", name = "page") int page,
			@RequestParam(defaultValue = "12", name = "size") int size,
			@RequestParam(value = "descending", defaultValue = "false") boolean descending) {

		List<LectureDto> lectureList = new ArrayList<>();
		// 필터링 : 제목 검색
		if (title != null && !title.isEmpty()) {
			lectureList = lectureService.searchLecturesByTitle(title);
		} else {
			lectureList = lectureService.getAllLectures();
		}
		// 필터링 : 조건에 따라 lectureList 필터링
		if (filter != null && !filter.isEmpty()) {
			lectureList = lectureService.filterLectures(lectureList, filter, descending);
		}
		// 필터링 : 지역 검색
		if(region != null && !region.isEmpty()){
			lectureList = lectureService.filterRegion(lectureList, region);
		}

		// 필터링 : 모집중 = 신청가능상태, 개설대기중 = 개설대기상태, 진행중 = 진행상태
		if (status != null && !status.isEmpty()) {
			LectureEntity.LectureStatus lectureStatus;

			switch (status) {
				case "모집중":
					lectureStatus = LectureEntity.LectureStatus.신청가능상태;
					break;
				case "개설대기중":
					lectureStatus = LectureEntity.LectureStatus.개설대기상태;
					break;
				case "진행중":
					lectureStatus = LectureEntity.LectureStatus.진행상태;
					break;
				default:
					throw new IllegalArgumentException("잘못된 상태 키워드입니다.");
			}

			lectureList = lectureService.filterStatus(lectureList, lectureStatus);
		}
		// 필터링 : 카테고리명
		if(category != null && !category.isEmpty()){
			lectureList = lectureService.filterCategory(lectureList, category);
		}

		// 검색결과에 해당하는 강좌가 없을경우
		if (lectureList.isEmpty()) {
			throw new NoSuchElementException("검색결과에 해당하는 강좌가 없습니다.ㅠㅠ");
		}

		// 페이징
		Pageable pageable = PageRequest.of(page, size);
		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), lectureList.size());
		Page<LectureDto> pagedLectureDto = new PageImpl<>(lectureList.subList(start, end), pageable, lectureList.size());
		return ResponseEntity.ok(pagedLectureDto);
	}

	private LectureDto convertToDto(LectureEntity lectureEntity) {
		if (lectureEntity == null)
			return null;

		return LectureDto.builder()
				.create_id(lectureEntity.getCreate_id())
				.creator(lectureEntity.getCreator())
				.max_participants(lectureEntity.getMaxParticipants())
				.category(lectureEntity.getCategory())
				.bank_name(lectureEntity.getBank_name())
				.account_name(lectureEntity.getAccount_name())
				.account_number(lectureEntity.getAccount_number())
				.price(lectureEntity.getPrice())
				.title(lectureEntity.getTitle())
				.content(lectureEntity.getContent())
				//.cycle(lectureEntity.getCycle())
				//.count(lectureEntity.getCount())
				.week(lectureEntity.getWeek())
				.learning_target(lectureEntity.getLearningTarget())
				//.attendance_requirements(lectureEntity.getAttendanceRequirements())
				.start_date(lectureEntity.getStart_date())
				.end_date(lectureEntity.getEnd_date())
				.region(lectureEntity.getRegion())
				.image_url(lectureEntity.getImage_url())
				.createdDate(lectureEntity.getCreatedDate())
				.build();
	}
}
