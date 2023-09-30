package com.seniorjob.seniorjobserver.service;

import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.dto.UserDto;
import com.seniorjob.seniorjobserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

    // 회원 전체목록
    public List<UserDto> getAllUsers() {
        List<UserEntity> userEntities = userRepository.findAll();
        if (userEntities.isEmpty()) {
            throw new IllegalArgumentException("회원가입된 회원이 없습니다..");
        }
        return userEntities.stream()
                .map(this::convertToDo)
                .collect(Collectors.toList());
    }

    // 로그인된 회원정보
    public UserDto getLoggedInUserDetails(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        // 로그인 되지 않았을 경우
        if ("anonymousUser".equals(userName)) {
            throw new IllegalStateException("로그인을 해주세요!");
        }

        UserEntity userEntity = userRepository.findByPhoneNumber(userName)
                .orElseThrow(()->new UsernameNotFoundException("유저를 찾을 수 없습니다.."));
        UserDto userDto = convertToDo(userEntity);
        //userDto.setEncryptionCode(null);
        return userDto;
    }

    // 회원정보 수정
    public UserDto updateUser(UserDto userDto){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        // 로그인 되지 않았을 경우
        if ("anonymousUser".equals(userName)) {
            throw new IllegalStateException("로그인을 해주세요!");
        }

        UserEntity userEntity = userRepository.findByPhoneNumber(userName)
                .orElseThrow(()->new UsernameNotFoundException("유저를 찾을 수 없습니다.."));
        userEntity.setName(Optional.ofNullable(userDto.getName()).orElse(userEntity.getName()));
        userEntity.setDateOfBirth(Optional.ofNullable(userDto.getDateOfBirth()).orElse(userEntity.getDateOfBirth()));
        userEntity.setJob(Optional.ofNullable(userDto.getJob()).orElse(userEntity.getJob()));
        userEntity.setRegion(Optional.ofNullable(userDto.getRegion()).orElse(userEntity.getRegion()));
        userEntity.setImgKey(Optional.ofNullable(userDto.getImgKey()).orElse(userEntity.getImgKey()));
        userEntity.setCategory(Optional.ofNullable(userDto.getCategory()).orElse(userEntity.getCategory()));

        userRepository.save(userEntity);
        return convertToDo(userEntity);
    }

    // 회원탈퇴 service

    private UserDto convertToDo(UserEntity userEntity) {
        return UserDto.builder()
                .uid(userEntity.getUid())
                .encryptionCode(userEntity.getEncryptionCode())
                .name(userEntity.getName())
                .dateOfBirth(userEntity.getDateOfBirth())
                .gender(Optional.ofNullable(userEntity.getGender()).orElse(UserEntity.Gender.기타))
                .loginType(Optional.ofNullable(userEntity.getLoginType()).orElse(UserEntity.LoginType.쇼셜))
                .userType(Optional.ofNullable(userEntity.getUserType()).orElse(UserEntity.UserType.개인))
                .phoneNumber(userEntity.getPhoneNumber())
                .job(userEntity.getJob())
                .region(userEntity.getRegion())
                .imgKey(userEntity.getImgKey())
                .category(userEntity.getCategory())
                .updateDate(userEntity.getUpdateDate())
                .createDate(userEntity.getCreateDate())
                .build();
    }
}
