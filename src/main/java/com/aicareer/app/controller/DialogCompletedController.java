package com.aicareer.app.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/dialog-completed")
public class DialogCompletedController {

  @GetMapping
  public String showDialogCompleted(HttpSession session, Model model) {
    if (session.getAttribute("authenticated") == null) {
      return "redirect:/login";
    }

    Boolean dialogCompleted = (Boolean) session.getAttribute("dialogCompleted");
    if (dialogCompleted == null || !dialogCompleted) {
      return "redirect:/send-message";
    }

    List<String> messageHistory = (List<String>) session.getAttribute("messageHistory");
    model.addAttribute("messageHistory", messageHistory);

    if (messageHistory != null) {
      int questionsCount = (messageHistory.size() + 1) / 2;
      model.addAttribute("questionsCount", questionsCount);
    }

    return "DialogCompleted";
  }

  @PostMapping
  public String restartDialog(HttpSession session) {
    session.removeAttribute("messageHistory");
    session.removeAttribute("dialogCompleted");
    session.removeAttribute("dialogEndTime");
    return "redirect:/send-message";
  }
}