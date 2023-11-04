package com.seniorjob.seniorjobserver.service;

import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.dto.UserDetailDto;
import com.seniorjob.seniorjobserver.dto.UserDto;
import com.seniorjob.seniorjobserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    // 회원가입 encryption_code = 비밀번호 암호화
    public UserEntity createUser(UserDto userDto){

        String encryptedPassword = passwordEncoder.encode(userDto.getEncryptionCode());
        userDto.setEncryptionCode(encryptedPassword);

        if(existsByPhoneNumber(userDto.getPhoneNumber())){
            throw new IllegalArgumentException("이미 가입된 전화번호입니다.");
        }
        UserEntity userEntity = userDto.toEntity();
        return userRepository.save(userEntity);
    }

    private boolean existsByPhoneNumber(String phoneNumber){
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    // 로그인
    public UserEntity authenticate(String phoneNumber, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(phoneNumber, password);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다."));
    }

    // 회원 전체목록
    public List<UserDetailDto> getAllUsers() {
        List<UserEntity> userEntities = userRepository.findAll();
        if (userEntities.isEmpty()) {
            throw new IllegalArgumentException("회원가입된 회원이 없습니다.");
        }
        return userEntities.stream()
                .map(this::convertToUserDetailDto)
                .collect(Collectors.toList());
    }

    // 로그인된 회원정보
    public UserDetailDto getLoggedInUserDetails(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        // 로그인 되지 않았을 경우
        if ("anonymousUser".equals(userName)) {
            throw new IllegalStateException("로그인을 해주세요!");
        }

        UserEntity userEntity = userRepository.findByPhoneNumber(userName)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다.."));
        UserDetailDto userDetailDto = convertToUserDetailDto(userEntity);
        return userDetailDto;
    }

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
