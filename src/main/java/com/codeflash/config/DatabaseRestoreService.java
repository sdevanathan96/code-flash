package com.codeflash.config;

import com.codeflash.repository.ProblemRepository;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DatabaseRestoreService implements ApplicationRunner {

  private final ProblemRepository problemRepository;

  @Value("${backup.pg-restore-path:/usr/bin/psql}")
  private String pgRestorePath;

  @Value("${backup.directory:${user.home}/codeflash-backups}")
  private String backupDirectory;

  @Value("${backup.restore.enabled:false}")
  private boolean restoreEnabled;

  @Value("${backup.db-name:codeflash}")
  private String dbName;

  @Value("${backup.db-user:postgres}")
  private String dbUser;

  @Override
  public void run(ApplicationArguments args) {
    try {
      if (!restoreEnabled) {
        log.info("Auto-restore disabled. Skipping.");
        return;
      }
      Optional<Path> latest = findLatestBackup();
      if (latest.isEmpty()) {
        log.warn("No backup files found in: {}", backupDirectory);
        return;
      }
      long count = problemRepository.count();
      if (count > 0) {
        log.info("DB already has data ({} problems). Skipping restore.", count);
        return;
      }
      restoreFromFile(latest.get());
    } catch (Exception e) {
      log.error("Restore failed — continuing with empty DB. Restore manually if needed.", e);
    }
  }

  private Optional<Path> findLatestBackup() {
    if (!Files.exists(Path.of(backupDirectory))) return Optional.empty();

    try (Stream<Path> files = Files.list(Path.of(backupDirectory))) {
       return files
           .filter(p -> p.toString().endsWith(".sql"))
           .max(Comparator.comparingLong(p -> p.toFile().lastModified()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  private void restoreFromFile(Path backupFile) throws IOException, InterruptedException {
    ProcessBuilder pb = new ProcessBuilder(
        pgRestorePath, "-U", dbUser, "-d", dbName, "-f", backupFile.toString()
    );
    pb.redirectError(ProcessBuilder.Redirect.INHERIT);

    Process process = pb.start();
    int exitCode = process.waitFor();

    if (exitCode == 0) {
       log.info("Restore completed successfully from: {}", backupFile);
    } else {
       log.error("Restore failed with exit code: {}. Manual intervention required.", exitCode);
    }
  }
}