package com.aicareer.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class LogoutController {

  @GetMapping("/logout")
  public String logout(HttpSession session) {
    if (session != null) {
      session.invalidate();
    }
    return "redirect:/login";
  }
}