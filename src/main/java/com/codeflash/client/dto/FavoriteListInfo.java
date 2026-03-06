package com.codeflash.client.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FavoriteListInfo(String idHash, String name) {}
