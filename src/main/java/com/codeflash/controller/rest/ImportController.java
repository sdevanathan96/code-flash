package com.codeflash.controller.rest;

import com.codeflash.client.LeetCodeGraphQLClient;
import com.codeflash.client.dto.FavoriteListInfo;
import com.codeflash.dto.ImportResult;
import com.codeflash.dto.request.ImportRequest;
import com.codeflash.service.ImportService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

  private final ImportService importService;
  private final LeetCodeGraphQLClient leetCodeClient;

  @PostMapping
  public ResponseEntity<ImportResult> runImport(@Valid @RequestBody ImportRequest request) {
    return ResponseEntity.ok(importService.runImport(request));
  }

  @GetMapping("/lists/available")
  public ResponseEntity<List<FavoriteListInfo>> fetchUserLists() {
    return ResponseEntity.ok(leetCodeClient.fetchUserLists());
  }
}
