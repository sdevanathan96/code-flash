package com.codeflash.dto;

import java.util.List;

public record RawProblemData(
    String slug,
    String title,
    String difficulty,
    List<String> tags,
    List<String> companyTags
) {}