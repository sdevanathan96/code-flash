package com.codeflash.dto.request;
import com.codeflash.service.importer.ImportSource;
import jakarta.validation.constraints.NotNull;

  public record ImportRequest(
     @NotNull(message = "Import source is required")
     ImportSource source,

     String param
  ) {}
