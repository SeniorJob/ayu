package com.seniorjob.seniorjobserver.controller;

import antlr.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seniorjob.seniorjobserver.config.CookieUtil;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.LectureProposalEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.dto.LoginRequest;
import com.seniorjob.seniorjobserver.dto.UserDetailDto;
import com.seniorjob.seniorjobserver.dto.UserDto;
import com.seniorjob.seniorjobserver.repository.*;
import com.seniorjob.seniorjobserver.service.StorageService;
import com.seniorjob.seniorjobserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
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
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository, PasswordEncoder pwEncoder,
                          StorageService storageService, ObjectMapper objectMapper,
                          LectureRepository lectureRepository, LectureApplyRepository lectureApplyRepository,
                          LectureProposalRepository lectureProposalRepository, LectureProposalApplyRepository lectureProposalApplyRepository,
                          WeekRepository weekRepository, WeekPlanRepository weekPlanRepository, AttendanceRepository attendanceRepository) {
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
    }

    // 회원가입API with 암호화 세션//
    @PostMapping("/join")
    public ResponseEntity<?> registerUser(
            @RequestParam("userDto") String userDtoJson){
            //@RequestParam("file") MultipartFile file)
        try {
            UserDto userDto = objectMapper.readValue(userDtoJson, UserDto.class);

            Map<String, String> requireInput = new HashMap<>();
            requireInput.put("이름 ", userDto.getName());
            requireInput.put("휴대폰번호 ", userDto.getPhoneNumber());
            requireInput.put("비밀번호 ", userDto.getEncryptionCode());
            requireInput.put("비밀번호 확인 ", userDto.getConfirmPassword());
            requireInput.put("직업 ", userDto.getJob());

            for (Map.Entry<String, String> entry : requireInput.entrySet()){
                if(entry.getValue() == null || entry.getValue().trim().isEmpty()){
                    return ResponseEntity.badRequest().body(entry.getKey() + "을(를) 입력해주세요!");
                }
            }

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

            // 전화번호확인
            if(!isValidPhoneNumber(userDto.getPhoneNumber())){
                return ResponseEntity.badRequest().body("전화번호는 앞 3자리를 포함하여 11자리를 입력해주세요!");
            }

//            String imageUrl = storageService.uploadImage(file);
//            userDto.setImgKey(imageUrl);
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

    // 숫자만 11자리인 형태로 전화번호 검증
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^\\d{11}$");
    }


    // 세션 로그인
    // POST /api/users/login
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        String phoneNumber = loginRequest.getPhoneNumber();
        String password = loginRequest.getPassword();

        if (phoneNumber == null || password == null || phoneNumber.trim().isEmpty() || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("아이디와 비밀번호를 입력해주세요");
        }

        try {
            UserEntity user = userService.authenticate(phoneNumber, password);
            HttpSession session = request.getSession(true);
            CookieUtil.addCookie(response, "SESSIONID", session.getId(), 24 * 60 * 60);

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", user.getName() + " 회원님 로그인에 성공하였습니다");
            return ResponseEntity.ok(responseBody);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("계정을 찾을 수 없습니다.");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 틀렸습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("로그인 처리 중 오류가 발생했습니다.");
        }
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 틀렸습니다.");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFound(UsernameNotFoundException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralError(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("내부 서버 오류: " + e.getMessage());
    }

    // 세션 로그아웃
    // POST /api/users/logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        CookieUtil.clearCookie(response, "SESSIONID");
        CookieUtil.clearCookie(response, "JSESSIONID");
        return ResponseEntity.ok("로그아웃에 성공하였습니다.");
    }

    // 회원전체목록API
    // GET /api/users/all
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserDetailDto> users = userService.getAllUsers();
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 로그인된 회원정보api
    // GET /api/users/detail
    @GetMapping("/detail")
    public ResponseEntity<?> getUserDetails() {
        try {
            UserDetailDto userDto = userService.getLoggedInUserDetails();
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
        UserDetailDto userDetailDto = objectMapper.readValue(userDtoJson, UserDetailDto.class);

        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 새 파일이 제공된 경우
        if (file != null && !file.isEmpty()) {
            if (currentUser.getImgKey() != null && !currentUser.getImgKey().isEmpty()) {
                storageService.deleteImage(currentUser.getImgKey()); // 기존 이미지 삭제
            }
            String imageUrl = storageService.uploadImage(file);
            userDetailDto.setImgKey(imageUrl);
        }

        try {
            UserDetailDto updatedUser = userService.updateUser(userDetailDto);
            return ResponseEntity.ok(updatedUser);
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
