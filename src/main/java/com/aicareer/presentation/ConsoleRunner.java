package com.aicareer.presentation;

import com.aicareer.application.CareerNavigatorApplication;
import com.aicareer.application.CareerNavigatorApplicationImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConsoleRunner implements CommandLineRunner {

  private final CareerNavigatorApplication application;

  @Override
  public void run(String... args) throws Exception {
    // Приведение к реализации, так как ConsolePresentation ожидает конкретный тип
    new ConsolePresentation((CareerNavigatorApplicationImpl) application).start();
  }
}