package com.codeflash.client.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FavoriteQuestionList(String name, List<QuestionDetail> questions) {}
