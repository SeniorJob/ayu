package com.seniorjob.seniorjobserver.viewController;

import com.seniorjob.seniorjobserver.dto.LectureDto;
import com.seniorjob.seniorjobserver.service.LectureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeViewController {

    @Autowired
    private LectureService lectureService;

    // main페이지 강좌목록
    @GetMapping("/")
    public String home(Model model){
        List<LectureDto> lectureList = lectureService.getAllLectures();
        model.addAttribute("lectures", lectureList);
        return "index";
    }

}
