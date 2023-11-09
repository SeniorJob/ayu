package com.seniorjob.seniorjobserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seniorjob.seniorjobserver.config.JwtTokenProvider;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.LectureProposalEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.dto.TokenDto;
import com.seniorjob.seniorjobserver.reponse.ErrorResponse;
import com.seniorjob.seniorjobserver.reponse.LoginResponse;
import com.seniorjob.seniorjobserver.dto.UserDetailDto;
import com.seniorjob.seniorjobserver.dto.UserDto;
import com.seniorjob.seniorjobserver.reponse.LogoutResponse;
import com.seniorjob.seniorjobserver.repository.*;
import com.seniorjob.seniorjobserver.service.StorageService;
import com.seniorjob.seniorjobserver.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;
    private final LectureRepository lectureRepository;
    private final LectureApplyRepository lectureApplyRepository;
    private final LectureProposalRepository lectureProposalRepository;
    private final LectureProposalApplyRepository lectureProposalApplyRepository;
    private final WeekRepository weekRepository;
    private final WeekPlanRepository weekPlanRepository;
    private final AttendanceRepository attendanceRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService, UserRepository userRepository, PasswordEncoder pwEncoder,
                          StorageService storageService, ObjectMapper objectMapper,
                          LectureRepository lectureRepository, LectureApplyRepository lectureApplyRepository,
                          LectureProposalRepository lectureProposalRepository, LectureProposalApplyRepository lectureProposalApplyRepository,
                          WeekRepository weekRepository, WeekPlanRepository weekPlanRepository, AttendanceRepository attendanceRepository,
                          AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.objectMapper = objectMapper;
        this.lectureRepository = lectureRepository;
        this.lectureApplyRepository = lectureApplyRepository;
        this.lectureProposalRepository = lectureProposalRepository;
        this.lectureProposalApplyRepository = lectureProposalApplyRepository;
        this.weekRepository = weekRepository;
        this.weekPlanRepository = weekPlanRepository;
        this.attendanceRepository = attendanceRepository;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // 회원가입API with 암호화 세션//
    @PostMapping("/join")
    public ResponseEntity<?> registerUser(@RequestBody UserDto userDto) {
        try {
            UserEntity userEntity = userService.createUser(userDto);
            return ResponseEntity.ok(userEntity);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // JWT로그인API
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDto userDto) {
        try {
            LoginResponse loginResponse = userService.login(userDto);
            return ResponseEntity.ok(loginResponse);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        }
    }

    // 회원전체목록API
    // GET /api/users/all
//    @GetMapping("/all")
//    public ResponseEntity<?> getAllUsers() {
//        try {
//            List<UserDetailDto> users = userService.getAllUsers();
//            return new ResponseEntity<>(users, HttpStatus.OK);
//        } catch (IllegalArgumentException e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.NO_CONTENT);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    // 로그인된 회원정보api
    // POST /api/users/detail
    @PostMapping("/detail")
    public ResponseEntity<?> getUserDetails() {
        try {
            // SecurityContext에서 인증 정보를 가져와 UserService에 전달
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailDto userDto = userService.getLoggedInUserDetails(authentication);
            return ResponseEntity.ok(userDto);
        } catch (IllegalStateException | UsernameNotFoundException e) {
            // 상태 코드와 함께 예외 메시지 반환
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            // 기타 예외 처리
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    // 회원정보수정API
    // PUT /api/users/update/(현재 Jwt로그인된 user의 정보를 불러와 수정)
    // PUT /api/users/update
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("userDto") String userDtoJson
    ) throws IOException {
        // 인증 정보에서 사용자명(전화번호) 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPhoneNumber = authentication.getName();

        // JSON 문자열을 UserDetailDto 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        UserDetailDto userDetailDto = objectMapper.readValue(userDtoJson, UserDetailDto.class);

        // 현재 로그인한 사용자의 정보를 조회
        UserEntity currentUser = userRepository.findByPhoneNumber(currentPhoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 새 이미지 파일이 있는 경우 처리
        if (file != null && !file.isEmpty()) {
            if (currentUser.getImgKey() != null && !currentUser.getImgKey().isEmpty()) {
                storageService.deleteImage(currentUser.getImgKey()); // 기존 이미지 삭제
            }
            String imageUrl = storageService.uploadImage(file);
            userDetailDto.setImgKey(imageUrl);
        }

        // 사용자 정보 업데이트 시도
        try {
            UserDetailDto updatedUser = userService.updateUser(userDetailDto);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalStateException | UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // 회원탈퇴 API
    // DELETE /api/users/delete (로그인후 이용자의 비밀번호 확인후 일치시 삭제)
    @DeleteMapping("/delete")
    @Transactional
    public ResponseEntity<?> deleteUser(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        String password = payload.get("password");
        try {
            UserEntity user = userRepository.findByPhoneNumber(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (!passwordEncoder.matches(password, user.getEncryptionCode())) {
                return ResponseEntity.badRequest().body("비밀번호가 일치하지 않습니다.");
            }

            // 강좌에 신청한 사용자의 신청 정보를 삭제
            lectureApplyRepository.deleteByUser(user);

            // 해당 사용자가 생성한 강좌를 가져옴
            List<LectureEntity> userLectures = lectureRepository.findByUser(user);
            for (LectureEntity lecture : userLectures) {
                // 강좌에 대한 모든 신청 정보를 삭제
                lectureApplyRepository.deleteByLecture(lecture);
            }
            lectureRepository.deleteAll(userLectures); // 강좌 삭제

            // 강좌 제안에 신청한 사용자의 신청 정보를 삭제
            lectureProposalApplyRepository.deleteByUser(user);

            // 해당 사용자가 생성한 강좌 제안을 가져옴
            List<LectureProposalEntity> userProposals = lectureProposalRepository.findByUser(user);
            for (LectureProposalEntity proposal : userProposals) {
                // 강좌 제안에 대한 모든 신청 정보를 삭제
                lectureProposalApplyRepository.deleteByLectureProposal(proposal);
            }
            lectureProposalRepository.deleteAll(userProposals); // 강좌 제안 삭제

            // 사용자를 삭제
            userRepository.delete(user);

            return ResponseEntity.ok("회원탈퇴에 성공하였습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
