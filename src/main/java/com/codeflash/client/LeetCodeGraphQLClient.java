package com.codeflash.client;

import com.codeflash.client.dto.FavoriteListInfo;
import com.codeflash.dto.RawProblemData;

import java.util.List;
import java.util.Optional;

public interface LeetCodeGraphQLClient {

  Optional<RawProblemData> fetchProblemMetadata(String slug);

  List<RawProblemData> fetchListProblems(String favoriteIdHash);

  List<FavoriteListInfo> fetchUserLists();

  List<RawProblemData> fetchStudyPlanProblems(String planSlug);
}