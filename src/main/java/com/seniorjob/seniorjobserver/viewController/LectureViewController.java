package com.seniorjob.seniorjobserver.viewController;

import com.seniorjob.seniorjobserver.dto.LectureDto;
import com.seniorjob.seniorjobserver.service.LectureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class LectureViewController {
    @Autowired
    private LectureService lectureService;
    @Autowired
    private RestTemplate restTemplate;

    // 강좌목록
    @GetMapping("/lecture/lectureList")
    public String getLectureList(Model model) {
        List<LectureDto> lectureList = lectureService.getAllLectures();
        model.addAttribute("lectures", lectureList);
        return "lectureList";
    }

    // 강좌상세보기
    @GetMapping("/lecture/detail/{id}")
    public String getLectureDetail(@PathVariable Long id, Model model) {
        LectureDto lecture = lectureService.getDetailLectureById(id);
        System.out.println(lecture);
        model.addAttribute("lecture", lecture);
        return "lectureDetail";
    }

    // 강좌개설
    @GetMapping("/lectureCreate")
    public String createLecture(){
        return "lectureCreate";
    }

    // 강좌 참여 신청을 처리하는 메소드
    @PostMapping("/lecture/apply/{lectureId}")
    public String applyForLecture(@PathVariable Long lectureId,
                                  @RequestParam(required = false) String applyReason,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        // 현재 로그인한 사용자 정보 가져오기
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // REST API에 강좌 참여 신청 요청 보내기
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("lectureId", lectureId);
        requestBody.put("applyReason", applyReason);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                "/api/lectureapply/apply/{lectureId}", // API 엔드포인트
                requestEntity,
                String.class,
                userDetails.getUsername() // 현재 사용자의 username (전화번호 등)
        );

        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
            // 성공적으로 신청된 경우
            redirectAttributes.addFlashAttribute("successMessage", "강좌 신청이 완료되었습니다.");
        } else {
            // 실패한 경우에 대한 처리
            redirectAttributes.addFlashAttribute("errorMessage", "강좌 신청 중 오류가 발생했습니다.");
        }

        // 신청 결과에 관계없이 강좌 상세 페이지로 리다이렉트
        return "redirect:/lecture/detail/{lectureId}";
    }
}
