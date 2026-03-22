package com.aicareer.app.controller;

import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.service.gigachat.GigaChatService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/send-message")
@RequiredArgsConstructor
public class DialogController {

  private final GigaChatService gigaChatService;

  @GetMapping
  public String showDialog(@RequestParam(value = "chatType", required = false) String chatType,
      HttpSession session,
      Model model) {
    if (session.getAttribute("authenticated") == null) {
      return "redirect:/login";
    }

    if ("clarification".equals(chatType)) {
      startClarificationChat(session);
    }

    setupMessageHistory(session, model);
    return "DialogService"; // /jsp/DialogService.jsp
  }

  @PostMapping
  public String sendMessage(@RequestParam("message") String message,
      HttpSession session,
      Model model) {
    if (session.getAttribute("authenticated") == null) {
      return "redirect:/login";
    }

    String userEmail = (String) session.getAttribute("userEmail");
    log.info("📨 Message from {}: {}", userEmail, message);

    String chatType = (String) session.getAttribute("currentChatType");

    if ("clarification".equals(chatType)) {
      return handleClarificationChat(session, message, model);
    }

    return handleRegularDialog(session, message, model);
  }

  private void startClarificationChat(HttpSession session) {
    String selectedField = (String) session.getAttribute("selectedField");
    String firstQuestion = generateFirstClarificationQuestion(selectedField);
    List<String> clarificationHistory = new ArrayList<>();
    clarificationHistory.add(firstQuestion);
    session.setAttribute("clarificationChatHistory", clarificationHistory);
    session.setAttribute("clarificationQuestionCount", 1);
    session.setAttribute("currentChatType", "clarification");
    session.setAttribute("messageHistory", clarificationHistory);
  }

  private String handleClarificationChat(HttpSession session, String message, Model model) {
    List<String> clarificationHistory = (List<String>) session.getAttribute("clarificationChatHistory");
    Integer questionCount = (Integer) session.getAttribute("clarificationQuestionCount");
    String selectedField = (String) session.getAttribute("selectedField");

    clarificationHistory.add(message);

    if (questionCount < 5) {
      String nextQuestion = generateNextClarificationQuestion(clarificationHistory, selectedField, questionCount);
      clarificationHistory.add(nextQuestion);
      session.setAttribute("clarificationQuestionCount", questionCount + 1);
      session.setAttribute("clarificationChatHistory", clarificationHistory);
      session.setAttribute("messageHistory", clarificationHistory);
      setupMessageHistory(session, model);
      return "DialogService";
    } else {
      completeClarificationAndGenerateRoadmap(session, clarificationHistory, selectedField);
      return "redirect:/career-roadmap";
    }
  }

  private String generateFirstClarificationQuestion(String field) {
    String prompt = "Пользователь выбрал направление: " + field +
        ". Он только что посмотрел 10 реальных вакансий с hh.ru в этой области. " +
        "Задай первый уточняющий вопрос чтобы понять его текущий уровень, опыт и цели. " +
        "Вопрос должен быть конкретным и помогать составить персонализированный roadmap.";
    return gigaChatService.sendMessage(prompt);
  }

  private String generateNextClarificationQuestion(List<String> history, String field, int currentQuestion) {
    StringBuilder chatContext = new StringBuilder();
    for (int i = 0; i < history.size(); i++) {
      if (i % 2 == 0) {
        chatContext.append("AI: ").append(history.get(i)).append("\n");
      } else {
        chatContext.append("User: ").append(history.get(i)).append("\n");
      }
    }

    String prompt = "История уточняющей беседы:\n" + chatContext +
        "\nНаправление: " + field +
        "\nЗадай следующий уточняющий вопрос (" + (currentQuestion + 1) + "/5). " +
        "Вопрос должен углублять понимание потребностей пользователя для составления roadmap.";
    return gigaChatService.sendMessage(prompt);
  }

  private void completeClarificationAndGenerateRoadmap(HttpSession session, List<String> history, String field) {
    StringBuilder fullDialog = new StringBuilder();
    for (int i = 0; i < history.size(); i++) {
      if (i % 2 == 0) {
        fullDialog.append("AI: ").append(history.get(i)).append("\n");
      } else {
        fullDialog.append("User: ").append(history.get(i)).append("\n");
      }
    }

    String roadmapPrompt = "На основе всего диалога и выбранного направления создай подробный roadmap.\n" +
        "Направление: " + field + "\n" +
        "Полный диалог:\n" + fullDialog + "\n\n" +
        "Создай структурированный план обучения с этапами, сроками и конкретными шагами.";

    String roadmap = gigaChatService.sendMessage(roadmapPrompt);
    session.setAttribute("finalRoadmap", roadmap);
  }

  private String handleRegularDialog(HttpSession session, String message, Model model) {
    List<String> messageHistory = (List<String>) session.getAttribute("messageHistory");
    if (messageHistory == null) {
      messageHistory = new ArrayList<>();
    }

    if (message != null && (message.equalsIgnoreCase("/complete") ||
        message.equalsIgnoreCase("/finish") ||
        message.equalsIgnoreCase("завершить"))) {
      return completeDialogAndRedirect(session, model);
    }

    if (!message.trim().isEmpty()) {
      messageHistory.add(message.trim());

      try {
        String prompt = buildPrompt(message, messageHistory);
        String aiResponse = gigaChatService.sendMessage(prompt);
        messageHistory.add(aiResponse);

        if (isDialogComplete(messageHistory)) {
          return completeDialogAndRedirect(session, model);
        }
      } catch (Exception e) {
        String fallbackResponse = "Извините, в настоящее время сервис AI временно недоступен.";
        messageHistory.add(fallbackResponse);
      }

      session.setAttribute("messageHistory", messageHistory);
    }

    setupMessageHistory(session, model);
    return "DialogService";
  }

  private String buildPrompt(String currentMessage, List<String> messageHistory) {
    StringBuilder prompt = new StringBuilder();

    prompt.append("Ты - AI помощник по карьерному развитию 'Career Navigator'. ");
    prompt.append("Твоя роль - помогать пользователям с вопросами карьеры, обучения и профессионального развития. ");
    prompt.append("Отвечай профессионально, но дружелюбно. Будь полезным и поддерживающим. ");
    prompt.append("Фокусируйся на карьерных темах: профориентация, навыки, обучение, поиск работы, карьерный рост. ");
    prompt.append("После ответа задай уточняющий вопрос по смежной теме. ");
    prompt.append("Если вопрос не по теме, вежливо направляй разговор в профессиональное русло.\n\n");

    if (messageHistory != null && messageHistory.size() > 1) {
      prompt.append("Контекст предыдущего диалога:\n");
      for (int i = 0; i < messageHistory.size() - 1; i += 2) {
        if (i < messageHistory.size() - 1) {
          prompt.append("Пользователь: ").append(messageHistory.get(i)).append("\n");
        }
        if (i + 1 < messageHistory.size() - 1) {
          prompt.append("AI: ").append(messageHistory.get(i + 1)).append("\n");
        }
      }
      prompt.append("\n");
    }

    prompt.append("Текущий вопрос пользователя: ").append(currentMessage).append("\n\n");
    prompt.append("Ответь на вопрос пользователя, учитывая контекст диалога:");

    return prompt.toString();
  }

  private boolean isDialogComplete(List<String> messageHistory) {
    return messageHistory != null && messageHistory.size() >= 6;
  }

  private String completeDialogAndRedirect(HttpSession session, Model model) {
    try {
      UserPreferences userPreferences = new UserPreferences();
      List<String> messageHistory = (List<String>) session.getAttribute("messageHistory");
      String userInfo = "Информация из диалога:\n";

      if (messageHistory != null && !messageHistory.isEmpty()) {
        for (int i = 0; i < messageHistory.size(); i += 2) {
          if (i < messageHistory.size()) {
            userInfo += "Пользователь: " + messageHistory.get(i) + "\n";
          }
          if (i + 1 < messageHistory.size()) {
            userInfo += "AI: " + messageHistory.get(i + 1) + "\n";
          }
        }
      } else {
        userInfo = "Пользователь прошел краткий диалог";
      }

      userPreferences.setInfoAboutPerson(userInfo);
      Long userId = (Long) session.getAttribute("userId");
      if (userId != null) {
        userPreferences.setUserId(userId);
      }
      session.setAttribute("userPreferences", userPreferences);
      log.info("✅ Диалог завершен, UserPreferences сохранены");
      return "redirect:/choose-vacancy";
    } catch (Exception e) {
      log.error("❌ Ошибка при завершении диалога", e);
      return "redirect:/send-message";
    }
  }

  private void setupMessageHistory(HttpSession session, Model model) {
    List<String> messageHistory = (List<String>) session.getAttribute("messageHistory");
    if (messageHistory != null) {
      model.addAttribute("messageHistory", messageHistory);
    }
  }
}