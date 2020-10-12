/**
 * ************************** FACIAL RECOGNITION - CAPSTONE************************
 * Controller - StudentController
 * This Controller is responsible for handling any request that is related to Students.
 * @author  STEALTH
 *
 */

package com.stealth.yash.FaceRecognition.controller;

import com.stealth.yash.FaceRecognition.model.AWSClient;
import com.stealth.yash.FaceRecognition.model.Student;
import com.stealth.yash.FaceRecognition.service.springdatajpa.DepartmentSDJpaService;
import com.stealth.yash.FaceRecognition.service.springdatajpa.ProgramSDJpaService;
import com.stealth.yash.FaceRecognition.service.springdatajpa.StudentSDJpaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;

// Causes lombok to generate a logger field
@Slf4j
// Indicates that this class serves the role of a controller
@Controller
// Will create the base URI /students for which the controller will be used
@RequestMapping("/students")


public class StudentController {

    private final StudentSDJpaService studentService;
    private final ProgramSDJpaService programService;
    private final DepartmentSDJpaService departmentSDJpaService;
    private final AWSClient amclient;
    String faceid="";

    /**
     * This is a student constructor
     * @param amclient this is an object of type AWSClient model
     * @param studentService - an object of type StudentSDJpaService service
     * @param programService - an object of type ProgramSDJpaService service
     * @param departmentSDJpaService - an object of type DepartmentSDJpaService service
     */
    public StudentController(AWSClient amclient,StudentSDJpaService studentService, ProgramSDJpaService programService, DepartmentSDJpaService departmentSDJpaService) {
        this.studentService = studentService;
        this.programService = programService;
        this.departmentSDJpaService = departmentSDJpaService;
        this.amclient = amclient;
    }

    /**
     * This method shows all the students
     * @param model - an object of type Model
     * @return student view
     */
    @GetMapping({"", "/"})
    public String getStudents(Model model) {
//        Student student = new Student();
//        String image = studentService.findById(student.getId()).getImage();
//        model.addAttribute("userImage",image);
        model.addAttribute("students", studentService.findAll());

        return "student/students";
    }


    /**
     * This method shows selected student
     * @param studentId an object for studentID of type Long
     * @param model an object of Model type
     * @return info of a particular student
     */
    @GetMapping("/get/{studentId}")
    public String showStudentInfo(@PathVariable Long studentId, Model model) throws UnsupportedEncodingException {
        Student student = new Student();
        String image = studentService.findById(studentId).getImage();
        model.addAttribute("userImage",image);
        model.addAttribute("student", studentService.findById(studentId));
        return "student/student-info";
    }

    /**
     * This method creates or updates student
     * @param studentId an object for studentID of type Long
     * @param model an object of Model type
     * @return creteOrUpdateStudent web page
     */
    @GetMapping({"/update/{studentId}", "/create"})
    public String createOrUpdateStudent(@PathVariable Optional<Long> studentId, Model model) {
        if (studentId.isPresent()){
            model.addAttribute("student",studentService.findById(studentId.get()));
        }else{
            Student student = new Student();
            model.addAttribute("student", student);
        }
        model.addAttribute("programs",programService.findAll());
        model.addAttribute("departments",departmentSDJpaService.findAll());
        return "student/createOrUpdateStudent";
    }

    /**
     * This method processes the data in Student Update form
     * @param student1 an object of Student model
     * @param bindingResult object of interface BindingResult
     * @param file object of a Multipart file
     * @return updated student info
     */
    @PostMapping(consumes = "multipart/form-data")
    public String processUpdateStudentForm(@Valid @ModelAttribute("student") Student student1, BindingResult bindingResult,@RequestPart(value = "file") MultipartFile file) {
        if(bindingResult.hasErrors()){
            bindingResult.getAllErrors().forEach(error -> log.error(error.toString()));
            return "student/createOrUpdateStudent";

        }

        if(!file.getContentType().equalsIgnoreCase("image/png")){
                System.out.println("Not a Proper Image type!!!");
        }else {
            student1.setImage(amclient.uploadFile(file));
            student1.setStuPasswordEmail(generatePassword());
            student1 = studentService.save(student1);
            String imagetoindex = studentService.findById(student1.getId()).getImage();
            String indexingimage = imagetoindex.substring(imagetoindex.lastIndexOf("/") + 1);
            faceid= amclient.addfacetoawscollection(indexingimage);
            // emailPasswordToUser(student1.getEmail(),student1.getStuPasswordEmail());
        }

       return "redirect:/students/get/" + student1.getId();
    }


    /**
     * This method generated password
     * @return the generated password
     *
     */
    public String generatePassword(){
        String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String password = "";
        int maxlength = 8;
        for (int i=0; i<maxlength; i++){
            Random rand = new Random();
            int index = rand.nextInt(str.length());
            password += str.charAt(index);
        }
        return password;
    }

    /**
     * This method emails passwords to users
     * @param to an object of type String
     * @param password an object of type String
     * @return user's password
     */
    public String emailPasswordToUser (String to, String password){

        String from = "stealtht90@gmail.com";
        String pass = "Sheridan123";
        Properties props = System.getProperties();
        String host = "smtp.gmail.com";
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", pass);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO,to);
            message.setSubject("Login Password - Stealth Admin");
            message.setText("Your password to access Stealth Admin Portal : " +password + "\n\n\nKind Regards,\n Team Stealth");
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
        return password;
    }

    /**
     * This method deletes a student
     * @param studentId an object of type Long
     * @return Students web page
     */
    @GetMapping("/delete/{studentId}")
    public String deleteStudent(@PathVariable Long studentId){
        Student student = new Student();
        this.amclient.removeFile(studentService.findById(studentId).getImage());
        this.amclient.deletefacefromawscollection(faceid);
        studentService.deleteById(studentId);
        return "redirect:/students";
    }

}
