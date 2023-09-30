package com.seniorjob.seniorjobserver.viewController;

import com.seniorjob.seniorjobserver.dto.LectureDto;
import com.seniorjob.seniorjobserver.service.LectureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class LectureViewController {

    @Autowired
    private LectureService lectureService;

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
        System.out.println(lecture); // Print to console for debugging
        model.addAttribute("lecture", lecture);
        return "lectureDetail";
    }

}
