package com.codeflash.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatabaseBackupService {

  @Value("${backup.directory:${user.home}/codeflash-backups}")
  private String backupDirectory;

  @Value("${backup.db-name:codeflash}")
  private String dbName;

  @Value("${backup.db-user:postgres}")
  private String dbUser;

  @Value("${backup.retention-days:30}")
  private int retentionDays;

  @PostConstruct
  public void ensureBackupDirectory() {
    try {
      Path dir = Paths.get(backupDirectory);
      if (!Files.exists(dir)) {
        Files.createDirectories(dir);
        log.info("Created backup directory: {}", backupDirectory);
      }
    } catch (IOException e) {
      log.error("Failed to create backup directory: {}", backupDirectory, e);
    }
  }

  @PreDestroy
  public void backupOnShutdown() {
    try {
      String filename = backupDirectory + "/backup_" + LocalDate.now() + ".sql";
      ProcessBuilder pb = new ProcessBuilder("pg_dump", "-U", dbUser, dbName);
      pb.redirectOutput(new File(filename));
      pb.redirectError(ProcessBuilder.Redirect.INHERIT);
      int exitCode = pb.start().waitFor();
      if (exitCode == 0) log.info("Backup created: {}", filename);
      else log.error("Backup failed with exit code: {}", exitCode);
      pruneOldBackups();
    } catch (IOException | InterruptedException e) {
      log.error("Backup failed on shutdown", e);
    }
  }
  private void pruneOldBackups() {
    try (Stream<Path> files = Files.list(Paths.get(backupDirectory))) {
       files.filter(p -> p.toString().endsWith(".sql"))
           .filter(p -> {
             try {
               return isOlderThan(p, retentionDays);
             } catch (IOException e) {
               log.warn("Could not check age of backup file: {}", p);
               return false;
             }
           })
            .forEach(p -> {
                try {
                    Files.delete(p);
                    log.info("Deleted old backup: {}", p);
                } catch (IOException e) {
                    log.warn("Could not delete old backup: {}", p);
                }
            });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  private boolean isOlderThan(Path file, int days) throws IOException {
    FileTime lastModified = Files.getLastModifiedTime(file);
    LocalDate fileDate = lastModified.toInstant()
       .atZone(ZoneId.systemDefault())
       .toLocalDate();
    return fileDate.isBefore(LocalDate.now().minusDays(days));
  }
}