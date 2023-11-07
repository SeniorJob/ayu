package com.seniorjob.seniorjobserver.service;

import com.seniorjob.seniorjobserver.config.JwtTokenProvider;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.exception.CustomLoginException;
import com.seniorjob.seniorjobserver.reponse.LoginResponse;
import com.seniorjob.seniorjobserver.dto.UserDetailDto;
import com.seniorjob.seniorjobserver.dto.UserDto;
import com.seniorjob.seniorjobserver.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // 회원가입 encryption_code = 비밀번호 암호화
    // 회원가입 encryption_code = 비밀번호 암호화
    public UserEntity createUser(UserDto userDto) {
        if (existsByPhoneNumber(userDto.getPhoneNumber())) {
            throw new IllegalStateException("이미 가입된 전화번호입니다.");
        }
        // 비밀번호 복잡성 검증을 위한 정규 표현식
        String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,12}$";

        // 비밀번호 검증
        if (!userDto.getEncryptionCode().matches(passwordRegex)) {
            throw new IllegalArgumentException("비밀번호는 6~12자리 영문+숫자 1개 이상이어야 합니다.");
        }
        if (!userDto.getEncryptionCode().equals(userDto.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 전화번호 검증
        String phoneRegex = "^010\\d{8}$";
        if (!userDto.getPhoneNumber().matches(phoneRegex)) {
            throw new IllegalArgumentException("유효하지 않은 전화번호 형식입니다. 010으로 시작하는 11자리 숫자여야 합니다.");
        }

        // 모든 필드 체크
        if (!StringUtils.hasText(userDto.getName()) || !StringUtils.hasText(userDto.getPhoneNumber()) ||
                userDto.getDateOfBirth() == null || !StringUtils.hasText(userDto.getEncryptionCode()) ||
                !StringUtils.hasText(userDto.getJob()) || !StringUtils.hasText(userDto.getRegion()) ||
                !StringUtils.hasText(userDto.getCategory())) {
            throw new IllegalArgumentException("모든 필수 항목을 입력해주세요!");
        }

        userDto.setEncryptionCode(passwordEncoder.encode(userDto.getEncryptionCode()));
        return userRepository.save(userDto.toEntity());
    }

    private boolean existsByPhoneNumber(String phoneNumber){
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    // 로그인
    public LoginResponse login(UserDto userDto) {
        UserEntity userEntity = userRepository.findByPhoneNumber(userDto.getPhoneNumber())
                .orElseThrow(() -> new CustomLoginException("전화번호를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(userDto.getEncryptionCode(), userEntity.getEncryptionCode())) {
            throw new CustomLoginException("비밀번호가 틀렸습니다.");
        }

        // Authentication 객체 생성 및 인증 처리
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDto.getPhoneNumber(), userDto.getEncryptionCode());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // JWT 토큰 생성 및 로그인 응답 반환
        String token = jwtTokenProvider.createToken(userDto.getPhoneNumber(), Collections.emptyList());
        return new LoginResponse(token, userEntity.getName() + "님이 로그인에 성공하였습니다.");
    }



    // 회원 전체목록
//    public List<UserDetailDto> getAllUsers() {
//        List<UserEntity> userEntities = userRepository.findAll();
//        if (userEntities.isEmpty()) {
//            throw new IllegalArgumentException("회원가입된 회원이 없습니다.");
//        }
//        return userEntities.stream()
//                .map(this::convertToUserDetailDto)
//                .collect(Collectors.toList());
//    }

    // 로그인된 회원정보
    public UserDetailDto getLoggedInUserDetails(Authentication authentication) {
        // 현재 인증된 사용자의 전화번호(또는 username)를 가져옵니다.
        String phoneNumber = authentication.getName();

        // 전화번호로 데이터베이스에서 사용자 정보를 조회합니다.
        UserEntity userEntity = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다."));

        // 조회된 UserEntity로부터 UserDetailDto를 생성하여 반환합니다.
        return convertToUserDetailDto(userEntity);
    }

//    public UserDetailDto getLoggedInUserDetails(){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String userName = authentication.getName();
//
//        // 로그인 되지 않았을 경우
//        if ("anonymousUser".equals(userName)) {
//            throw new IllegalStateException("로그인을 해주세요!");
//        }
//
//        UserEntity userEntity = userRepository.findByPhoneNumber(userName)
//                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다.."));
//        UserDetailDto userDetailDto = convertToUserDetailDto(userEntity);
//        return userDetailDto;
//    }

    // 회원정보 수정
    public UserDetailDto updateUser(UserDetailDto userDetailDto){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        // 로그인 되지 않았을 경우
        if ("anonymousUser".equals(userName)) {
            throw new IllegalStateException("로그인을 해주세요!");
        }

        // 모든 필드 체크
        if(userDetailDto.getName() == null || userDetailDto.getDateOfBirth() == null ||
                userDetailDto.getJob() == null || userDetailDto.getRegion() == null ||
                userDetailDto.getCategory() == null) {
            throw new IllegalArgumentException("5개 항목을 모두 입력해주세요!");
        }

        UserEntity userEntity = userRepository.findByPhoneNumber(userName)
                .orElseThrow(()->new UsernameNotFoundException("유저를 찾을 수 없습니다.."));
        userEntity.setName(Optional.ofNullable(userDetailDto.getName()).orElse(userEntity.getName()));
        userEntity.setDateOfBirth(Optional.ofNullable(userDetailDto.getDateOfBirth()).orElse(userEntity.getDateOfBirth()));
        userEntity.setJob(Optional.ofNullable(userDetailDto.getJob()).orElse(userEntity.getJob()));
        userEntity.setRegion(Optional.ofNullable(userDetailDto.getRegion()).orElse(userEntity.getRegion()));
        userEntity.setImgKey(Optional.ofNullable(userDetailDto.getImgKey()).orElse(userEntity.getImgKey()));
        userEntity.setCategory(Optional.ofNullable(userDetailDto.getCategory()).orElse(userEntity.getCategory()));

        userRepository.save(userEntity);
        return convertToUserDetailDto(userEntity);
    }

    private UserDto convertToDo(UserEntity userEntity) {
        return UserDto.builder()
                .uid(userEntity.getUid())
                .name(userEntity.getName())
                .phoneNumber(userEntity.getPhoneNumber())
                .encryptionCode(userEntity.getEncryptionCode())
                .job(userEntity.getJob())
                .dateOfBirth(userEntity.getDateOfBirth())
                .category(userEntity.getCategory())
                .region(userEntity.getRegion())
                .imgKey(userEntity.getImgKey())
                .updateDate(userEntity.getUpdateDate())
                .createDate(userEntity.getCreateDate())
                .build();
    }

    private UserDetailDto convertToUserDetailDto(UserEntity userEntity) {
        return UserDetailDto.builder()
                .uid(userEntity.getUid())
                .name(userEntity.getName())
                .phoneNumber(userEntity.getPhoneNumber())
                .encryptionCode(userEntity.getEncryptionCode())
                .job(userEntity.getJob())
                .dateOfBirth(userEntity.getDateOfBirth())
                .category(userEntity.getCategory())
                .region(userEntity.getRegion())
                .imgKey(userEntity.getImgKey())
                .createDate(userEntity.getCreateDate())
                .updateDate(userEntity.getUpdateDate())
                .build();
    }
}
