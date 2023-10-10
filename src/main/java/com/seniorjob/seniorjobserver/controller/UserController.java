package com.seniorjob.seniorjobserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.LectureProposalEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.dto.UserDto;
import com.seniorjob.seniorjobserver.repository.*;
import com.seniorjob.seniorjobserver.service.StorageService;
import com.seniorjob.seniorjobserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.time.LocalDateTime;
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
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository, PasswordEncoder pwEncoder, StorageService storageService, ObjectMapper objectMapper, LectureRepository lectureRepository, LectureApplyRepository lectureApplyRepository, LectureProposalRepository lectureProposalRepository, LectureProposalApplyRepository lectureProposalApplyRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.objectMapper = objectMapper;
        this.lectureRepository = lectureRepository;
        this.lectureApplyRepository = lectureApplyRepository;
        this.lectureProposalRepository = lectureProposalRepository;
        this.lectureProposalApplyRepository = lectureProposalApplyRepository;
    }

    // 구직자/사업주 회원가입API with 암호화 세션//
    @PostMapping("/join")
    public ResponseEntity<?> registerUser(
            @RequestParam("userDto") String userDtoJson,
            @RequestParam("file") MultipartFile file) {
        try {
            UserDto userDto = objectMapper.readValue(userDtoJson, UserDto.class);

            String encryptionCode = userDto.getEncryptionCode();
            String confirmPassword = userDto.getConfirmPassword();

            // 비밀번호 검증 메서드 호출
            if (!isValidPassword(encryptionCode)) {
                return ResponseEntity.badRequest().body("비밀번호는 6~12자리 영문+숫자 1개 이상이어야 합니다.");
            }

            // 비밀번호 확인 비교
            if (!encryptionCode.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body("비밀번호가 일치하지 않습니다!!");
            }

            String imageUrl = storageService.uploadImage(file);
            userDto.setImgKey(imageUrl);
            userDto.setCreateDate(LocalDateTime.now());

            UserEntity userEntity = userService.createUser(userDto);
            return ResponseEntity.ok(userEntity);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image: " + e.getMessage());
        }
    }

    // 비밀번호 생성 규칙 검증 메서드
    private boolean isValidPassword(String password) {
        // 비밀번호는 6~12자리 영문+숫자 1개 이상이어야 함
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d).{6,12}$");
    }

    // 세션 로그인
    // POST /api/users/login
    @PostMapping("/login")
    public ResponseEntity<?> loginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();

        UserEntity user = userRepository.findByPhoneNumber(currentUserName)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다. 회원가입을 진행해주세요!"));

        return ResponseEntity.ok(user.getName() + " 회원님 로그인에 성공하였습니다");
    }

    // 세션 로그아웃
    // POST /api/users/logout

    // 회원전체목록API
    // GET /api/users/all
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserDto> users = userService.getAllUsers();
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NO_CONTENT); // 회원이 없을 때의 메시지를 반환합니다.
        } catch (Exception e) {
            // 로깅 추가
            e.printStackTrace(); // 이것은 임시 로깅입니다. 실제 프로덕션에서는 SLF4J, Logback 등의 로깅 프레임워크를 사용하여 로그를 남기는 것이 좋습니다.
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); // 그 외의 에러 메시지를 반환합니다.
        }
    }

    // 로그인된 회원정보api
    // GET /api/users/detail
    @GetMapping("/detail")
    public ResponseEntity<?> getUserDetails() {
        try {
            UserDto userDto = userService.getLoggedInUserDetails();
            return ResponseEntity.ok(userDto);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("에러 : " + e.getMessage());
        }
    }

    // 회원정보수정API
    // PUT /api/users/update/(현재로그인된 user의 정보를 불러와 수정)
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("userDto") String userDtoJson,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        UserDto userDto = objectMapper.readValue(userDtoJson, UserDto.class);

        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 새 파일이 제공된 경우
        if (file != null && !file.isEmpty()) {
            if (currentUser.getImgKey() != null && !currentUser.getImgKey().isEmpty()) {
                storageService.deleteImage(currentUser.getImgKey()); // 기존 이미지 삭제
            }
            String imageUrl = storageService.uploadImage(file);
            userDto.setImgKey(imageUrl);
        }

        try {
            UserDto updateUser = userService.updateUser(userDto);
            return ResponseEntity.ok(updateUser);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (UsernameNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("에러 : " + e.getMessage());
        }
    }

    // 회원탈퇴 API
    // PUT /api/users/delete (로그인후 이용자의 비밀번호 확인후 일치시 삭제)
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
