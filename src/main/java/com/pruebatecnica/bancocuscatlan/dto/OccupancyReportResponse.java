package com.pruebatecnica.bancocuscatlan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupancyReportResponse {

    private Long spaceId;
    private String spaceName;
    private double occupancyPercentage;
}
