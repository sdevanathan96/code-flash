package com.codeflash.service.importer;

import com.codeflash.client.LeetCodeGraphQLClient;
import com.codeflash.config.EnrichmentConfig;
import com.codeflash.config.LeetCodeConfig;
import com.codeflash.entity.AppSettingsEntity;
import com.codeflash.entity.ProblemEntity;
import com.codeflash.repository.AppSettingsRepository;
import com.codeflash.repository.ProblemRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(4)
@RequiredArgsConstructor
public class PatternListEnricher implements ApplicationRunner {

  private static final String LAST_ENRICHED_KEY = "last_enriched_at";
  private static final int ENRICH_INTERVAL_DAYS = 1;

  private final ProblemRepository problemRepository;
  private final ProblemPersistenceHelper persistenceHelper;
  private final LeetCodeGraphQLClient client;
  private final LeetCodeConfig config;
  private final AppSettingsRepository settingsRepository;
  private final EnrichmentConfig enrichmentConfig;
  private final AtomicBoolean running = new AtomicBoolean(false);

  @Override
  public void run(ApplicationArguments args) {
    runEnrichmentIfDue();
  }

  @Scheduled(fixedDelay = 6 * 60 * 60 * 1000)
  public void scheduledEnrichment() {
    if (!running.compareAndSet(false, true)) {
      log.info("Enrichment already in progress, skipping.");
      return;
    }
    try {
      runEnrichmentIfDue();
    } finally {
      running.set(false);
    }
  }

  private void runEnrichmentIfDue() {
    if (!enrichmentConfig.isEnabled()) return;

    if (config.getSessionCookie() == null || config.getSessionCookie().isBlank()) {
      log.info("No LeetCode session cookie. Skipping enrichment.");
      return;
    }

    Optional<AppSettingsEntity> lastEnriched =
        settingsRepository.findById(LAST_ENRICHED_KEY);

    if (lastEnriched.isPresent()) {
      LocalDateTime lastTime = LocalDateTime.parse(lastEnriched.get().getValue());
      long daysSince = ChronoUnit.DAYS.between(lastTime, LocalDateTime.now());
      if (daysSince < enrichmentConfig.getIntervalDays()) {
        log.info("Enrichment not due. Last run {} day(s) ago.", daysSince);
        return;
      }
    }

    log.info("Starting enrichment...");

    try {
      persistenceHelper.enrichAllProblems(client);

      settingsRepository.save(new AppSettingsEntity(
          LAST_ENRICHED_KEY,
          LocalDateTime.now().toString()
      ));
      log.info("Enrichment timestamp updated.");

    } catch (Exception e) {
      log.error("Enrichment failed — timestamp not updated, will retry next run.", e);
    }
  }
}