package com.codeflash.controller.ui;

import com.codeflash.dto.response.*;
import com.codeflash.repository.ProblemListRepository;
import com.codeflash.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
class ImportViewController {

  private final com.codeflash.client.LeetCodeGraphQLClient leetCodeClient;
  private final ProblemListRepository problemListRepository;

  @GetMapping("/import")
  public String importPage(Model model) {
    model.addAttribute("importedLists", problemListRepository.findAll());

    try {
      model.addAttribute("availableLists", leetCodeClient.fetchUserLists());
    } catch (Exception e) {
      model.addAttribute("availableLists", List.of());
      model.addAttribute("apiError",
          "Could not fetch LeetCode lists. Check your session cookie in application.yml.");
    }

    return "import";
  }
}
