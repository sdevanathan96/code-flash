// ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
// ┃  FILE 1: config/LeetCodeConfig.java                                     ┃
// ┃  Purpose: Typed config for LeetCode API — session cookie + base URL     ┃
// ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
//
// ═══════════════════════════════════════════════════════════════════════════
//  CONCEPTUAL DEEP DIVE: @ConfigurationProperties vs @Value
// ═══════════════════════════════════════════════════════════════════════════
//
// @Value("${leetcode.session-cookie}") works for a single property.
// When you have a group of related properties, @ConfigurationProperties
// is cleaner — it binds the whole group to a typed object at once.
//
//   application.yml:
//     leetcode:
//       session-cookie: "abc123..."
//       base-url: "https://leetcode.com/graphql"
//       timeout-seconds: 10
//
//   @ConfigurationProperties(prefix = "leetcode"):
//     sessionCookie → leetcode.session-cookie
//     baseUrl       → leetcode.base-url
//     timeoutSeconds→ leetcode.timeout-seconds
//
// Spring Boot auto-converts kebab-case YAML keys to camelCase fields.
// You get a fully populated, type-safe object injected wherever needed.
//
// Advantages over scattered @Value:
//   - All LeetCode config in one place — easier to find and change
//   - IDE autocomplete works for application.yml keys
//   - @Validated enables JSR-303 validation at startup
//     (e.g., fail fast if session-cookie is blank)
//
// ── THINK ABOUT ───────────────────────────────────────────────────────────
// Q: The session cookie is sensitive. Should it be in application.yml?
// A: For a personal local app, application.yml is fine as long as the
//    file is in .gitignore. Never commit it to a public repo.
//    A safer approach: use an environment variable:
//
//      leetcode:
//        session-cookie: ${LEETCODE_SESSION:}
//
//    Then set it in your shell: export LEETCODE_SESSION="abc123..."
//    The :} default means it's empty string if not set — the app starts
//    but API calls will fail with a clear auth error rather than crashing.
// ──────────────────────────────────────────────────────────────────────────

package com.codeflash.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "leetcode")
public class LeetCodeConfig {

  private String sessionCookie;
  private String baseUrl = "https://leetcode.com/graphql";
  private int timeoutSeconds = 10;
}