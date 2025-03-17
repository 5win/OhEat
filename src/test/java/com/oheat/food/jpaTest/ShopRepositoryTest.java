package com.oheat.food.jpaTest;

import static com.oheat.common.SidogunguFixture.jongno_gu;
import static com.oheat.common.SidogunguFixture.seoul;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.oheat.common.TestConfig;
import com.oheat.common.sigungu.SigunguJpaRepository;
import com.oheat.food.dto.Coordinates;
import com.oheat.food.entity.CategoryJpaEntity;
import com.oheat.food.entity.ShopJpaEntity;
import com.oheat.food.repository.CategoryJpaRepository;
import com.oheat.food.repository.ShopJpaRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Import(TestConfig.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class ShopRepositoryTest {

    @Autowired
    private ShopJpaRepository shopJpaRepository;
    @Autowired
    private CategoryJpaRepository categoryJpaRepository;
    @Autowired
    private SigunguJpaRepository sigunguJpaRepository;
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void reset() {
        entityManager.createNativeQuery("ALTER TABLE shop AUTO_INCREMENT=1")
            .executeUpdate();
    }

    @Test
    @DisplayName("매장 등록 정보에 카테고리가 null이면 등록 실패")
    void shopNotContainsCategory_thenFail() {
        assertThrows(DataIntegrityViolationException.class, () -> {
            shopJpaRepository.save(
                ShopJpaEntity.builder()
                    .name("bbq")
                    .minimumOrderAmount(14_000)
                    .build()
            );
        });
        entityManager.clear();
    }

    @Test
    @DisplayName("이름이 중복되지 않으면, 매장 등록 성공")
    void shopNameNotDuplicate_thenSuccess() {
        CategoryJpaEntity category = CategoryJpaEntity.builder()
            .name("치킨")
            .build();
        ShopJpaEntity shop = ShopJpaEntity.builder()
            .name("bbq")
            .category(category)
            .address("서울특별시 종로구")
            .latitude(37.0)
            .longitude(127.0)
            .sido(seoul())
            .sigungu(jongno_gu())
            .build();

        categoryJpaRepository.save(category);
        sigunguJpaRepository.save(jongno_gu());

        assertDoesNotThrow(() -> shopJpaRepository.save(shop));
    }

    @Test
    @DisplayName("이름이 중복되면, 매장 등록 실패")
    void shopNameDuplicate_thenFail() {
        CategoryJpaEntity category = CategoryJpaEntity.builder()
            .name("치킨")
            .build();
        ShopJpaEntity shop = ShopJpaEntity.builder()
            .name("bbq")
            .category(category)
            .address("서울특별시 종로구")
            .minimumOrderAmount(14_000)
            .latitude(37.0)
            .longitude(127.0)
            .sido(seoul())
            .sigungu(jongno_gu())
            .build();

        categoryJpaRepository.save(category);
        sigunguJpaRepository.save(jongno_gu());
        shopJpaRepository.save(shop);

        assertThrows(DataIntegrityViolationException.class, () -> {
            shopJpaRepository.save(
                ShopJpaEntity.builder()
                    .name("bbq")
                    .category(category)
                    .address("서울특별시 종로구")
                    .latitude(37.0)
                    .longitude(127.0)
                    .sido(seoul())
                    .sigungu(jongno_gu())
                    .build());
        });
        entityManager.clear();
    }

    @Test
    @DisplayName("같은 카테고리의 매장이 3개 있을 때, JPA로 해당 카테고리의 매장 조회 시 size는 3이어야 함")
    void usingJpa_givenThreeShop_whenFindByCategory_thenListSizeThree() {
        CategoryJpaEntity category = CategoryJpaEntity.builder().name("치킨").build();
        categoryJpaRepository.save(category);

        sigunguJpaRepository.save(jongno_gu());

        for (int i = 0; i < 3; i++) {
            shopJpaRepository.save(ShopJpaEntity.builder()
                .name("bbq " + i + "호점")
                .category(category)
                .address("서울특별시 종로구")
                .latitude(37.0)
                .longitude(127.0)
                .sido(seoul())
                .sigungu(jongno_gu())
                .build());
        }

        PageRequest page0 = PageRequest.of(0, 5);
        Page<ShopJpaEntity> result = shopJpaRepository.findByCategory(category, List.of(1), page0);

        assertThat(result.getContent().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("치킨 매장이 7개이고 5개 단위로 페이징할 때, 조회 결과는 0번 페이지는 5개, 1번 페이지는 2개이며, 전체 집합 크기는 7개이다")
    void givenSevenShops_whenFindShopByCategory_thenPage0Return5AndPage1Return2() {
        CategoryJpaEntity category = CategoryJpaEntity.builder().name("치킨").build();
        categoryJpaRepository.save(category);

        sigunguJpaRepository.save(jongno_gu());

        for (int i = 0; i < 7; i++) {
            shopJpaRepository.save(ShopJpaEntity.builder()
                .name("bbq " + i + "호점")
                .category(category)
                .address("서울특별시 종로구")
                .latitude(37.0)
                .longitude(127.0)
                .sido(seoul())
                .sigungu(jongno_gu())
                .build());
        }

        PageRequest page0 = PageRequest.of(0, 5);
        PageRequest page1 = PageRequest.of(1, 5);
        Page<ShopJpaEntity> result1 = shopJpaRepository
            .findByCategory(category, List.of(1), page0);
        Page<ShopJpaEntity> result2 = shopJpaRepository
            .findByCategory(category, List.of(1), page1);

        assertThat(result1.getContent().size()).isEqualTo(5);
        assertThat(result2.getContent().size()).isEqualTo(2);
        assertThat(result1.getTotalElements()).isEqualTo(7);
        assertThat(result2.getTotalElements()).isEqualTo(7);
    }

    @Test
    @DisplayName("카테고리로 매장 목록 조회 시 정렬 기준이 기본순이면, 최근 등록 순으로 조회")
    void whenSortByDefault_thenRecentRegistrationOrder() {
        CategoryJpaEntity category = CategoryJpaEntity.builder().name("치킨").build();
        categoryJpaRepository.save(category);

        sigunguJpaRepository.save(jongno_gu());

        for (int i = 1; i <= 3; i++) {
            shopJpaRepository.save(ShopJpaEntity.builder()
                .name("bbq " + i + "호점")
                .category(category)
                .address("서울특별시 종로구")
                .latitude(37.0)
                .longitude(127.0)
                .sido(seoul())
                .sigungu(jongno_gu())
                .build());
        }

        PageRequest page0 = PageRequest.of(0, 5, Sort.by("id").descending());
        List<ShopJpaEntity> result = shopJpaRepository.findByCategory(category, List.of(1), page0)
            .getContent();

        assertThat(result.get(0).getId()).isEqualTo(3L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(2).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("카테고리로 매장 목록 조회 시 정렬 기준이 배달팁 낮은 순이면, 배달팁 오름차순으로 조회")
    void whenSortByDeliveryTip_thenDeliveryTipAscendingOrder() {
        CategoryJpaEntity category = CategoryJpaEntity.builder().name("치킨").build();
        categoryJpaRepository.save(category);

        sigunguJpaRepository.save(jongno_gu());

        for (int i = 1; i <= 3; i++) {
            shopJpaRepository.save(ShopJpaEntity.builder()
                .name("bbq " + i + "호점")
                .category(category)
                .deliveryFee(1000 * (5 - i))
                .address("서울특별시 종로구")
                .latitude(37.0)
                .longitude(127.0)
                .sido(seoul())
                .sigungu(jongno_gu())
                .build());
        }

        PageRequest page0 = PageRequest.of(0, 5, Sort.by("deliveryFee").ascending());
        List<ShopJpaEntity> result = shopJpaRepository.findByCategory(category, List.of(1), page0)
            .getContent();

        assertThat(result.get(0).getId()).isEqualTo(3L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(2).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("카테고리로 매장 목록 조회 시 정렬 기준이 최소주문 금액 낮은 순이면, 최소주문 금액 오름차순으로 조회")
    void whenSortByMinimumOrderAmount_thenMinimumOrderAmountAscendingOrder() {
        CategoryJpaEntity category = CategoryJpaEntity.builder().name("치킨").build();
        categoryJpaRepository.save(category);

        sigunguJpaRepository.save(jongno_gu());

        for (int i = 1; i <= 3; i++) {
            shopJpaRepository.save(ShopJpaEntity.builder()
                .name("bbq " + i + "호점")
                .category(category)
                .minimumOrderAmount(1000 * (5 - i))
                .address("서울특별시 종로구")
                .latitude(37.0)
                .longitude(127.0)
                .sido(seoul())
                .sigungu(jongno_gu())
                .build());
        }

        PageRequest page0 = PageRequest.of(0, 5, Sort.by("minimumOrderAmount").ascending());
        List<ShopJpaEntity> result = shopJpaRepository.findByCategory(category, List.of(1), page0)
            .getContent();

        assertThat(result.get(0).getId()).isEqualTo(3L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(2).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("정렬 기준이 최소주문 금액 낮은 순이면, 같은 금액일 때 id 내림차순(최신 등록순)으로 정렬")
    void whenSortByMinimumOrderAmountAndId_thenMinimumOrderAmountAscendingAndIdDescendingOrder() {
        CategoryJpaEntity category = CategoryJpaEntity.builder().name("치킨").build();
        categoryJpaRepository.save(category);

        sigunguJpaRepository.save(jongno_gu());

        for (int i = 1; i <= 3; i++) {
            shopJpaRepository.save(ShopJpaEntity.builder()
                .name("bbq " + i + "호점")
                .category(category)
                .minimumOrderAmount(1000 * i)
                .address("서울특별시 종로구")
                .latitude(37.0)
                .longitude(127.0)
                .sido(seoul())
                .sigungu(jongno_gu())
                .build());
        }

        PageRequest page0 = PageRequest.of(0, 5, Sort.by("minimumOrderAmount").ascending()
            .and(Sort.by("id").descending()));
        List<ShopJpaEntity> result = shopJpaRepository.findByCategory(category, List.of(1), page0)
            .getContent();

        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(2).getId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("카테고리로 매장 목록 조회 시 정렬 기준이 가까운 순이면, 내 위치에서의 거리 오름차순으로 조회")
    void whenSortByNearestOrder_thenDistanceInMyLocationAscendingOrder() {
        CategoryJpaEntity category = CategoryJpaEntity.builder().name("치킨").build();
        categoryJpaRepository.save(category);

        sigunguJpaRepository.save(jongno_gu());

        for (int i = 1; i <= 3; i++) {
            shopJpaRepository.save(ShopJpaEntity.builder()
                .name("bbq " + i + "호점")
                .category(category)
                .address("서울특별시 종로구")
                .latitude(37.0 + 0.1 * i)
                .longitude(127.0)
                .sido(seoul())
                .sigungu(jongno_gu())
                .build());
        }

        Coordinates coordinates = Coordinates.builder()
            .latitude(37.3)
            .longitude(127.3)
            .build();

        PageRequest distanceOrder = PageRequest.of(0, 5, Sort.by("distance")
            .and(Sort.by("id").descending()));

        List<ShopJpaEntity> result = shopJpaRepository.findByCategoryOrderByDistance(category, coordinates,
                distanceOrder)
            .getContent();

        assertThat(result.get(0).getId()).isEqualTo(3L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(2).getId()).isEqualTo(1L);
    }

    @Disabled
    @Test
    @DisplayName("카테고리로 매장 목록 조회 시 정렬 기준이 주문 많은 순이면, 누적 주문 수 내림차순으로 조회")
    void whenSortByOrderAmount_thenOrderAmountDescendingOrder() {

    }

    @Disabled
    @Test
    @DisplayName("카테고리로 매장 목록 조회 시 정렬 기준이 별점 높은 순이면, 별점 내림차순으로 조회")
    void whenSortByReviewScore_thenReviewScoreDescendingOrder() {

    }
}
