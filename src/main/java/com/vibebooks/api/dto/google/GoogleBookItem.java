package com.vibebooks.api.dto.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleBookItem(VolumeInfo volumeInfo) {}