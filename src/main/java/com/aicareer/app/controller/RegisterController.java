package com.aicareer.app.controller;

import com.aicareer.core.dto.user.UserRegistrationDto;
import com.aicareer.core.service.user.UserService;
import com.aicareer.core.service.user.model.RegistrationResult;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegisterController {

  private final UserService userService;

  @GetMapping
  public String showRegisterForm(Model model) {
    model.addAttribute("userRegistrationDto", new UserRegistrationDto());
    return "register";
  }

  @PostMapping
  public String processRegister(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
      BindingResult result,
      @RequestParam("confirmPassword") String confirmPassword,
      HttpSession session,
      Model model) {
    if (!registrationDto.getPassword().equals(confirmPassword)) {
      result.rejectValue("password", "error.user", "Пароли не совпадают");
    }

    if (result.hasErrors()) {
      return "register";
    }

    RegistrationResult regResult = userService.registerUser(registrationDto);
    if (regResult.isSuccess()) {
      return "redirect:/login?registered=true&email=" + registrationDto.getEmail();
    } else {
      model.addAttribute("errors", regResult.getErrors());
      model.addAttribute("userRegistrationDto", registrationDto);
      return "register";
    }
  }
}