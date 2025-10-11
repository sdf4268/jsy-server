package com.jeonsy.resume.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/") // 사용자가 웹사이트 루트 URL로 접속하면
    public String dashboard() {
        return "dashboard"; // /resources/templates/dashboard.html 파일을 보여준다
    }
    
    @GetMapping("/st-devices")
    public String smartThingsDevicesPage() {
        return "st-devices"; // templates/st-devices.html 파일을 찾아서 보여줍니다.
    }
}