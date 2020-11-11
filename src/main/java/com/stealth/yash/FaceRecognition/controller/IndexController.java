/**
 ************************** FACIAL RECOGNITION - CAPSTONE ************************
 * This Controller is responsible for handling index page requests
 * @author  STEALTH
 *
 */
package com.stealth.yash.FaceRecognition.controller;

import com.stealth.yash.FaceRecognition.model.Student;
import com.stealth.yash.FaceRecognition.service.springdatajpa.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
public class IndexController {

   private final InstituteSDJpaService instituteSDJpaService;
   private final DepartmentSDJpaService departmentSDJpaService;
   private final ProgramSDJpaService programSDJpaService;
   private final ProfessorSDJpaService professorSDJpaService;
   private final CourseSDJpaService courseSDJpaService;
   private final StudentSDJpaService studentSDJpaService;


    /**
     * This is a student constructor
     * @param instituteSDJpaService this is an object of type InstituteSDJpaService service
     * @param departmentSDJpaService - an object of type DepartmentSDJpaService service
     * @param programSDJpaService - an object of type ProgramSDJpaService service
     * @param studentSDJpaService - an object of type StudentSDJpaService service
     */

    public IndexController(InstituteSDJpaService instituteSDJpaService, DepartmentSDJpaService departmentSDJpaService, ProgramSDJpaService programSDJpaService, ProfessorSDJpaService professorSDJpaService, CourseSDJpaService courseSDJpaService, StudentSDJpaService studentSDJpaService) {
        this.instituteSDJpaService = instituteSDJpaService;
        this.departmentSDJpaService = departmentSDJpaService;
        this.programSDJpaService = programSDJpaService;
        this.professorSDJpaService = professorSDJpaService;
        this.courseSDJpaService = courseSDJpaService;
        this.studentSDJpaService = studentSDJpaService;
    }

    /**
     * This method shows main page
     * @return index page
     */
    @GetMapping({"/", "", "/index"})
    public String showMainPage(){
        return "index";

    }
    /**
     * This method displays contact information
     * @return contact web page
     */
    @GetMapping("/contact")
    public String contactUs(){
        return "contact";

    }
    /**
     * This method displays information about us
     * @return about web page
     */
    @GetMapping("/about")
    public String aboutUs(){
        return "about";

    }
    /**
     * This method directs to login page
     * @return login web page
     */
    @GetMapping("/login")
    public String login(){
        return "login";

    }
    @GetMapping("/student")
    public String student(){
        return "student";

    }


    @GetMapping("/dashboard")
    public String showDashboard(Model model){
        List<String> studentNames = new ArrayList<>();
        Set<Student> students = studentSDJpaService.findAll();
        for(Student student :students){
            studentNames.add(student.getFirstName());
        }


        model.addAttribute("institutes", instituteSDJpaService.findAll());
        model.addAttribute("departments", departmentSDJpaService.findAll());
        model.addAttribute("programs", programSDJpaService.findAll());
        model.addAttribute("professors", professorSDJpaService.findAll());
        model.addAttribute("courses", courseSDJpaService.findAll());
        model.addAttribute("students", students);
        model.addAttribute("studentNames", studentNames);


        return "dashboard";
    }

    /**
     * This method displays coming soon section on Index page
     * @return index page
     */
    @GetMapping("/comingsoon")
    public String comingSoon(){
       return "comingsoon/index";
    }

    /**
     * This method manages pagination
     * @param pageNo an object of type int
     * @param model an object of type Model
     * @return students web page
     */
   // @GetMapping("/getStudents/{pageNo}")
    public String findPaginated(@PathVariable (value = "pageNo") int pageNo,
                                Model model) {
        int pageSize = 5;

        Page<Student> page = studentSDJpaService.findPaginated(pageNo, pageSize);
        List<Student> listEmployees = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("listEmployees", listEmployees);
        return "students";
    }




}