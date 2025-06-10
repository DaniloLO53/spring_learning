//package org.example.security2.controllers;
//
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/sec")
//public class HelloController {
//    @GetMapping("/public")
//    public String getHello() {
//        return "Hello, public!";
//    }
//
//    @GetMapping("/private")
//    public String getPrivate(Authentication auth) {
//        return "This is private! Hello, " + auth.getName();
//    }
//}
