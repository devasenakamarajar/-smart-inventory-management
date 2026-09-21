package com.inventory.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

        @GetMapping({"/dashboard", "/products", "/products/add", "/categories", "/staff", "/customers",
            "/purchases", "/purchases/create", "/sales", "/sales/create", "/inventory", "/inventory/transactions",
            "/inventory/low-stock", "/returns", "/reports", "/reports/sales/today"})
    public String index() {
        return "index";
    }

    @GetMapping({"/", "/splash"})
    public String splash() {
        return "splash";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }
}
