package com.seniorjob.seniorjobserver.viewController;

import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.dto.UserDto;
import com.seniorjob.seniorjobserver.repository.*;
import com.seniorjob.seniorjobserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class UserViewController {

    private UserService userService;
    private final UserRepository userRepository;
    @Autowired
    public UserViewController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/view")
    public String viewUsers(Model model) {
        List<UserDto> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "users";
    }

    @GetMapping("/main")
    public String mainPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            UserEntity user = userRepository.findByPhoneNumber(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다. : " + userDetails.getUsername()));
            model.addAttribute("user", user);
            // 로그인 성공 후 회원 관리 페이지로 이동
            return "redirect:/user/userpage";
        }
        return "main";
    }

    // 유저페이지
    @GetMapping("/user/userpage")
    public String userPage() {
        return "user/userpage";
    }

    // 자기정보
    @GetMapping("/user/info")
    public String userInfo(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {

            model.addAttribute("userName", userDetails.getUsername());

            UserEntity user = userRepository.findByPhoneNumber(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다. : " + userDetails.getUsername()));

            model.addAttribute("user", user);
        }
        return "user/info";
    }

    // 회원가입
    @GetMapping("/register")
    public String showRegistrationForm(){
        return "register";
    }

    // 마이페이지
    @GetMapping("/user/mypage")
    public String mePage(){
        return "user/mypage";
    }
}
