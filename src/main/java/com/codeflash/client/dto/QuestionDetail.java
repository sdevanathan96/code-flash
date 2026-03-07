package com.codeflash.client.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QuestionDetail(
    @JsonProperty("titleSlug") String slug,
    String title,
    String difficulty,
    List<TopicTag> topicTags,
    List<CompanyTag> companyTags
) {}
