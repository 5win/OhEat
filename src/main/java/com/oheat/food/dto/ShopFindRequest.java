package com.oheat.food.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class ShopFindRequest {

    private final String categoryName;
    private final Double latitude;
    private final Double longitude;
    private final String sigungu;
}
