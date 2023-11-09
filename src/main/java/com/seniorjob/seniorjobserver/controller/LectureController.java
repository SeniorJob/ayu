package com.seniorjob.seniorjobserver.controller;

import com.amazonaws.services.kms.model.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.seniorjob.seniorjobserver.config.JwtTokenProvider;
import com.seniorjob.seniorjobserver.dto.FilterLectureDto;
import com.seniorjob.seniorjobserver.dto.LectureDetailDto;
import com.seniorjob.seniorjobserver.dto.RecommendLectureDto;
import com.seniorjob.seniorjobserver.repository.LectureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lectures")
public class LectureController {
	private final LectureService lectureService;
	private final StorageService storageService;
	private final UserRepository userRepository;
	private final LectureRepository lectureRepository;
	private final UserService userService;
	private static final Logger log = LoggerFactory.getLogger(LectureController.class);
	private AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;

	public LectureController(LectureService lectureService, StorageService storageService,
							 UserRepository userRepository, UserService userService, LectureRepository lectureRepository,
							 AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
		this.lectureService = lectureService;
		this.storageService = storageService;
		this.userRepository = userRepository;
		this.userService = userService;
		this.lectureRepository = lectureRepository;
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	// 강좌개설1단계 API
	// POST /api/lectures
	@PreAuthorize("isAuthenticated()")
	@PostMapping
	public ResponseEntity<LectureDto> createLecture(
			@RequestParam("file") MultipartFile file,
			@RequestParam("lectureDto") String lectureDtoJson,
			@AuthenticationPrincipal UserDetails userDetails
	) throws IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentPhoneNumber = authentication.getName();

		UserEntity currentUser = userRepository.findByPhoneNumber(currentPhoneNumber)
				.orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		LectureDto lectureDto = objectMapper.readValue(lectureDtoJson, LectureDto.class);

		lectureDto.setCreator(currentUser.getName()); // 로그인된 사용자의 이름을 creator로 지정

		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("강좌 대표 이미지를 선택해주세요!");
		}

		// 이미지 업로드
		String imageUrl = storageService.uploadImage(file);
		lectureDto.setImage_url(imageUrl);

		LectureDto createdLecture = lectureService.createLecture(lectureDto, currentUser);

		if (createdLecture.getUid() != null) {
			log.info("DTO에 할당된 사용자: {}", createdLecture.getUid().toString());
		} else {
			log.warn("DTO에 유저가 할당되지 않았습니다.");
		}

		return ResponseEntity.ok(createdLecture);
	}

	// 강좌개설 1단계 정보를 불러와 수정API Controller
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
	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/delete/{create_id}")
	public ResponseEntity<?> deleteMyLecture(
			@PathVariable Long create_id,
			@AuthenticationPrincipal UserDetails userDetails) {
		try {
			String lectureTitle = lectureService.deleteLectureCreatedByUser(create_id, userDetails.getUsername());
			return ResponseEntity.ok().body(lectureTitle + "이(가) 삭제되었습니다.");
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// 개설된강좌상세정보API
	// GET /api/lectures/detail/{id}
	@GetMapping("/detail/{id}")
	public ResponseEntity<LectureDetailDto> getLectureDetail(@PathVariable("id") Long id) {
		LectureDetailDto lectureDetailDto = lectureService.getDetailLectureById(id);
		return new ResponseEntity<>(lectureDetailDto, HttpStatus.OK);
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
	public ResponseEntity<Page<FilterLectureDto>> filterAndPaginateLectures(
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "filter", required = false) String filter,
			@RequestParam(value = "region", required = false) String region,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "category", required = false) String category,
			@RequestParam(defaultValue = "1", name = "page") int page,
			@RequestParam(defaultValue = "12", name = "size") int size,
			@RequestParam(value = "descending", defaultValue = "true") boolean descending) {

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

		// 기본값 "모집중" 강좌만 필터링
		// 필터링 : 모집중 = 신청가능상태, 개설대기중 = 개설대기상태, 진행중 = 진행상태, 완료강좌 = 완료상태
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
				case "완료강좌":
					lectureStatus = LectureEntity.LectureStatus.완료상태;
				default:
					throw new IllegalArgumentException("잘못된 상태 키워드입니다.");
			}

			lectureList = lectureService.filterStatus(lectureList, lectureStatus);
		}
		// 필터링 : 카테고리명
		if(category != null && !category.isEmpty()){
			lectureList = lectureService.filterCategory(lectureList, category);
		}

		// "철회상태" 제외 처리 추가
		lectureList = lectureService.excludeWithdrawnStatus(lectureList);

		// 검색결과에 해당하는 강좌가 없을경우
		if (lectureList.isEmpty()) {
			throw new NoSuchElementException("검색결과에 해당하는 강좌가 없습니다.ㅠㅠ");
		}

		// 페이징
		int zeroBasedPage = page -1;
		Pageable pageable = PageRequest.of(zeroBasedPage, size);
		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), lectureList.size());
		List<FilterLectureDto> filteredLectureList = lectureList.stream()
				.map(FilterLectureDto::from)
				.collect(Collectors.toList());

		Page<FilterLectureDto> filteredLectureDto = new PageImpl<>(filteredLectureList.subList(start, end), pageable, filteredLectureList.size());
		return ResponseEntity.ok(filteredLectureDto);
	}

	// 인가강좌API - '신청가능상태'의 강좌중 current_participants가 많은순으로 지정한 갯수만큼 출력된다.
	@GetMapping("/popular")
	public ResponseEntity<List<RecommendLectureDto>> getPopularLectures(
			@RequestParam(required = true) Integer limit) {
		List<RecommendLectureDto> popularLectures = lectureService.getPopularLecturesAsRecommend(limit);
		if(popularLectures.isEmpty()){
			throw new NotFoundException("현재 신청가능상태 = 모집중 상태의 강좌가 없습니다.");
		}
		return ResponseEntity.ok(popularLectures);
	}

	// 추천강좌API - 로그인한 회원의 카테고리를 기준으로 해당 카테고리가 일치하는 강좌중
	// '신청가능상태'의 강좌의 current_participants가 많은순으로 지정한 갯수많큼 출력된다.
	@GetMapping("/recommendLecture")
	public ResponseEntity<List<RecommendLectureDto>> getRecommendLectures(
			@RequestParam int limit,
			@AuthenticationPrincipal UserDetails userDetails){

		UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

		List<RecommendLectureDto> recommendLectures = lectureService.getRecommendedLectures(limit, currentUser.getCategory(), currentUser.getName());

		// 추천 강좌의 수가 limit 미만일 경우, 부족한 부분을 인기 강좌로 채움
		if (recommendLectures.size() < limit) {
			int needed = limit - recommendLectures.size();
			List<RecommendLectureDto> popularLecturesConverted = lectureService.getPopularLecturesAsRecommend(needed);
			if(!popularLecturesConverted.isEmpty()){
				recommendLectures.addAll(popularLecturesConverted);
			}
		}

		return ResponseEntity.ok(recommendLectures);
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
				.week(lectureEntity.getWeek())
				.learning_target(lectureEntity.getLearningTarget())
				.start_date(lectureEntity.getStart_date())
				.end_date(lectureEntity.getEnd_date())
				.region(lectureEntity.getRegion())
				.image_url(lectureEntity.getImage_url())
				.createdDate(lectureEntity.getCreatedDate())
				.build();
	}
}
