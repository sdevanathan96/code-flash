package com.codeflash.client;

import com.codeflash.client.dto.*;
import com.codeflash.config.LeetCodeConfig;
import com.codeflash.dto.RawProblemData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class LeetCodeGraphQLClientImpl implements LeetCodeGraphQLClient {

  private final WebClient webClient;
  private final ObjectMapper objectMapper;
  private final int timeoutSeconds;

  private static final String FETCH_QUESTION_QUERY = """
    query getQuestion($titleSlug: String!) {
        question(titleSlug: $titleSlug) {
            titleSlug
            title
            difficulty
            topicTags { name }
            companyTags { name slug }
        }
    }
    """;

  private static final String FETCH_LIST_PROBLEMS_QUERY = """
     query getFavoriteQuestionList($favoriteIdHash: String!) {
         favoriteQuestionList(favoriteIdHash: $favoriteIdHash) {
             name
             questions {
                 titleSlug
                 title
                 difficulty
                 topicTags { name }
             }
         }
     }
     """;

  private static final String FETCH_USER_LISTS_QUERY = """
     query getFavoritesLists {
         favoritesLists {
             allFavorites {
                 idHash
                 name
             }
         }
     }
     """;

  private static final String FETCH_STUDY_PLAN_QUERY = """
     query getStudyPlan($slug: String!) {
         studyPlanV2Detail(planSlug: $slug) {
             name
             questions {
                 titleSlug
                 title
                 difficulty
                 topicTags { name }
             }
         }
     }
     """;

  public LeetCodeGraphQLClientImpl(LeetCodeConfig config, ObjectMapper objectMapper){
    this.objectMapper = objectMapper;
    this.timeoutSeconds = config.getTimeoutSeconds();
    this.webClient = WebClient.builder()
        .baseUrl(config.getBaseUrl())
        .defaultHeader("Content-Type", "application/json")
        .defaultHeader("Referer", "https://leetcode.com")
        .defaultHeader("Cookie",
            "LEETCODE_SESSION=" + config.getSessionCookie() +
                "; csrftoken=" + config.getCsrfToken())  // ← add csrftoken
        .defaultHeader("x-csrftoken", config.getCsrfToken())  // ← also needed
        .build();
  }

  @Override
  public Optional<RawProblemData> fetchProblemMetadata(String slug) {
    Map<String, Object> body = Map.of(
        "query",     FETCH_QUESTION_QUERY,
        "variables", Map.of("titleSlug", slug)
    );
    try {
      String json = executeQuery(body);
      GraphQLResponse<QuestionData> response = objectMapper.readValue(
          json, new TypeReference<>() {}
      );
      QuestionDetail q = response.data().question();
      if (q == null) return Optional.empty();
      return Optional.of(toRawProblemData(q));
    } catch (WebClientResponseException.Forbidden w) {
      log.error("Session cookie expired or missing.");
      return Optional.empty();
    } catch (Exception e) {
      log.error("Failed to parse LeetCode API response for slug: {}", slug, e);
      return Optional.empty();
    }
  }
  @Override
  public List<RawProblemData> fetchListProblems(String favoriteIdHash) {
    Map<String, Object> body = Map.of(
        "query",     FETCH_LIST_PROBLEMS_QUERY,
        "variables", Map.of("favoriteIdHash", favoriteIdHash)
    );
    try {
      String json = executeQuery(body);
      GraphQLResponse<FavoriteListData> response = objectMapper.readValue(
          json, new TypeReference<>() {}
      );
      List<QuestionDetail> questions = response.data().favoriteQuestionList().questions();
      if (questions == null) return List.of();
      return questions.stream().map(this::toRawProblemData).toList();
    } catch (WebClientResponseException.Forbidden w) {
      log.error("Session cookie expired or missing.");
      return List.of();
    } catch (Exception e) {
      log.error("Failed to parse LeetCode API response for hash: {}", favoriteIdHash, e);
      return List.of();
    }
  }

  @Override
  public List<FavoriteListInfo> fetchUserLists(){
    Map<String, Object> body = Map.of(
        "query",     FETCH_USER_LISTS_QUERY
    );
    try {
      String json = executeQuery(body);
      GraphQLResponse<FavoritesListsData> response = objectMapper.readValue(
          json, new TypeReference<>() {}
      );
      List<FavoriteListInfo> q = response.data().favoritesLists().allFavorites();
      if (q == null) return List.of();
      return q;
    } catch (WebClientResponseException.Forbidden w) {
      log.error("Session cookie expired or missing.");
      return List.of();
    } catch (Exception e) {
      log.error("Failed to parse LeetCode API response for user lists", e);
      return List.of();
    }
  }
  @Override
  public List<RawProblemData> fetchStudyPlanProblems(String planSlug) {
    Map<String, Object> body = Map.of(
        "query",     FETCH_STUDY_PLAN_QUERY,
        "variables", Map.of("slug", planSlug)
    );
    try {
      String json = executeQuery(body);
      GraphQLResponse<StudyPlanData> response = objectMapper.readValue(
          json, new TypeReference<>() {}
      );
      List<QuestionDetail> questions = response.data().studyPlanV2Detail().questions();
      if (questions == null) return List.of();
      return questions.stream().map(this::toRawProblemData).toList();
    } catch (WebClientResponseException.Forbidden w) {
      log.error("Session cookie expired or missing.");
      return List.of();
    } catch (Exception e) {
      log.error("Failed to parse LeetCode API response for slug: {}", planSlug, e);
      return List.of();
    }
  }

  private String executeQuery(Map<String, Object> body) {
    return webClient.post()
       .bodyValue(body)
       .retrieve()
       .bodyToMono(String.class)
       .block(Duration.ofSeconds(timeoutSeconds));
  }

  private RawProblemData toRawProblemData(QuestionDetail q) {
    List<String> tags = q.topicTags() == null ? List.of() :
        q.topicTags().stream().map(TopicTag::name).toList();
    List<String> companies = q.companyTags() == null ? List.of() :
        q.companyTags().stream().map(CompanyTag::name).toList();
    return new RawProblemData(q.slug(), q.title(), q.difficulty(), tags, companies);
  }
}
