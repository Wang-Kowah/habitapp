package com.kowah.habitapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/")
public class CommonServiceController {

    @RequestMapping(value = "/index")
    public String index() {
        return "/index.html";
    }
}
