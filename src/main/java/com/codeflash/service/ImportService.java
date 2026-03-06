package com.codeflash.service;

import com.codeflash.dto.ImportResult;
import com.codeflash.dto.request.ImportRequest;
import com.codeflash.service.importer.ImportSource;
import com.codeflash.service.importer.ImporterFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportService {

  private final ImporterFactory importerFactory;

  public ImportResult runImport(ImportRequest request){
    if (request.source() != ImportSource.GRIND_150
            && (request.param() == null || request.param().isBlank())) {
        throw new IllegalArgumentException(
            "param is required for source: " + request.source());
    }
    var importer = importerFactory.create(request.source(), request.param());
    ImportResult result = importer.importProblems();
    log.info("Import complete for source {}: {}", request.source(), result);
    return result;
    }
}
