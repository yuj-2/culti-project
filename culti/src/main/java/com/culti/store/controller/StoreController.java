package com.culti.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller // RestController가 아님에 주의!
@RequestMapping("/store")
public class StoreController {

    @GetMapping("/main")
    public String storeMain() {
        return "store/storeMain"; // templates/store/storeMain.html 호출
    }
}