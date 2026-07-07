package com.pruebatecnica.bancocuscatlan.integration;

import com.pruebatecnica.bancocuscatlan.TestcontainersConfiguration;
import com.pruebatecnica.bancocuscatlan.config.CacheConfig;
import com.pruebatecnica.bancocuscatlan.dto.OccupancyReportResponse;
import com.pruebatecnica.bancocuscatlan.repository.SpaceRepository;
import com.pruebatecnica.bancocuscatlan.service.ReportService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Regresión del bug de caché: con la config rota (spring.cache.cache-names:
 * default + literal "occupancyReport" en @Cacheable) la primera llamada ya
 * fallaba con IllegalArgumentException. Este test también verifica que la
 * segunda llamada se sirve desde caché (no vuelve a golpear el repositorio).
 */
@SpringBootTest(properties = "spring.profiles.active=test")
@Import(TestcontainersConfiguration.class)
class ReportServiceCacheIntegrationTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoSpyBean
    private SpaceRepository spaceRepository;

    @AfterEach
    void clearCache() {
        cacheManager.getCache(CacheConfig.OCCUPANCY_REPORT_CACHE).clear();
    }

    @Test
    void getOccupancyReport_secondCallIsServedFromCache() {
        LocalDate from = LocalDate.of(2026, 7, 1);
        LocalDate to = LocalDate.of(2026, 7, 2);

        List<OccupancyReportResponse> first = reportService.getOccupancyReport(from, to);
        List<OccupancyReportResponse> second = reportService.getOccupancyReport(from, to);

        assertThat(second).isEqualTo(first);
        verify(spaceRepository, times(1)).findByActiveTrue();
    }
}
