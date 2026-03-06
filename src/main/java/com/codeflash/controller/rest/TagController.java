
package com.codeflash.controller.rest;

import com.codeflash.dto.response.TagResponse;
import com.codeflash.repository.ProblemRepository;
import com.codeflash.repository.TagRepository;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

  private final TagRepository tagRepository;
  private final ProblemRepository problemRepository;

  @GetMapping
  public ResponseEntity<List<TagResponse>> getTags() {
    List<TagResponse> tags = tagRepository.findAll().stream()
        .map(t -> new TagResponse(
            t.getId(), t.getName(),
            problemRepository.findByTagName(t.getName()).size()
        ))
        .sorted(Comparator.comparing(TagResponse::name))
        .toList();
    return ResponseEntity.ok(tags);
  }
}
