package com.oheat.food.serviceTest;

import static com.oheat.common.SidogunguFixture.jongno_gu;
import static com.oheat.common.SidogunguFixture.seoul;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.oheat.common.sido.SidoRepository;
import com.oheat.common.sigungu.SigunguRepository;
import com.oheat.food.dto.ShopFindRequest;
import com.oheat.food.dto.ShopFindResponse;
import com.oheat.food.dto.ShopSaveRequest;
import com.oheat.food.dto.ShopUpdateRequest;
import com.oheat.food.entity.CategoryJpaEntity;
import com.oheat.food.entity.ShopJpaEntity;
import com.oheat.food.exception.CategoryNotExistsException;
import com.oheat.food.exception.DuplicateShopNameException;
import com.oheat.food.exception.ShopNotExistsException;
import com.oheat.food.fake.MemoryCategoryRepository;
import com.oheat.food.fake.MemoryShopRepository;
import com.oheat.food.repository.CategoryRepository;
import com.oheat.food.repository.ShopRepository;
import com.oheat.food.service.ShopService;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class ShopServiceTest {

    private ShopService shopService;
    private final ShopRepository memoryShopRepository = new MemoryShopRepository();
    private final CategoryRepository memoryCategoryRepository = new MemoryCategoryRepository();

    private final SidoRepository sidoRepository = Mockito.mock(SidoRepository.class);
    private final SigunguRepository sigunguRepository = Mockito.mock(SigunguRepository.class);

    @BeforeEach
    void setUp() {
        shopService = new ShopService(memoryShopRepository, memoryCategoryRepository,
            sidoRepository, sigunguRepository);
    }

    @Test
    @DisplayName("매장 이름 중복되지 않으면 매장 등록 성공")
    void shopNameNotDuplicate_thenSuccess() {
        CategoryJpaEntity category = CategoryJpaEntity.builder().name("치킨").build();
        memoryCategoryRepository.save(category);

        when(sidoRepository.findByCtpKorNm("서울특별시"))
            .thenReturn(Optional.of(seoul()));
        when(sigunguRepository.findBySigKorNm("종로구"))
            .thenReturn(Optional.of(jongno_gu()));

        assertDoesNotThrow(() -> {
            shopService.registerShop(
                ShopSaveRequest.builder()
                    .name("bbq")
                    .category("치킨")
                    .sido("서울특별시")
                    .sigungu("종로구")
                    .build()
            );
        });
    }

    @Test
    @DisplayName("매장 이름이 중복되면 매장 등록 실패")
    void shopNameDuplicate_thenFail() {
        memoryShopRepository.save(
            ShopJpaEntity.builder()
                .name("오잇")
                .build());

        assertThrows(DuplicateShopNameException.class, () -> {
            shopService.registerShop(
                ShopSaveRequest.builder()
                    .name("오잇")
                    .build());
        });
    }

    @Test
    @DisplayName("카테고리가 null이면 매장 등록 실패")
    void whenCategoryIsNull_thenFail() {
        assertThrows(RuntimeException.class, () -> {
            shopService.registerShop(ShopSaveRequest.builder()
                .name("bbq")
                .category(null)
                .minimumOrderAmount(14_000)
                .build());
        });
    }

    @Test
    @DisplayName("존재하지 않는 카테고리이면 매장 등록 실패")
    void whenCategoryNotExists_thenFail() {
        assertThrows(CategoryNotExistsException.class, () -> {
            shopService.registerShop(ShopSaveRequest.builder()
                .name("bbq")
                .category("치킨")
                .minimumOrderAmount(14_000)
                .build());
        });
    }

    @Disabled
    @Test
    @DisplayName("매장 이름에 숫자, 한글, 영어 이외의 글자가 포함되면 실패")
    void shopNameContainsOnlyNumberKoreanEnglish() {

    }

    @Test
    @DisplayName("치킨 매장 3개를 등록하고 치킨 카테고리에 해당하는 매장을 조회하면, 리스트 size가 3이어야 함")
    void givenThreeChickenShops_whenFindChickenShop_thenListSizeThree() {
        CategoryJpaEntity category = CategoryJpaEntity.builder().name("치킨").build();
        memoryCategoryRepository.save(category);

        when(sidoRepository.findByCtpKorNm("서울특별시"))
            .thenReturn(Optional.of(seoul()));
        when(sigunguRepository.findBySigKorNm("종로구"))
            .thenReturn(Optional.of(jongno_gu()));

        for (int i = 0; i < 3; i++) {
            shopService.registerShop(ShopSaveRequest.builder()
                .name("bbq " + i + "호점")
                .category("치킨")
                .sido("서울특별시")
                .sigungu("종로구")
                .build());
        }

        ShopFindRequest findReq = ShopFindRequest.builder()
            .categoryName("치킨")
            .sigungu("종로구")
            .build();

        PageRequest pageable = PageRequest.of(0, 5, Sort.by("id").descending());
        Page<ShopFindResponse> result = shopService.findShopByCategory(findReq, pageable);

        Assertions.assertThat(result.getContent().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("Pageable 정렬 특성이 최소주문금액 낮은 순이면, 최소주문금액 오름차순으로 매장이 조회됨")
    void whenFindShopByCategoryOrderByMiniMumOrderAmount_thenReturnMinimumOrderAmountAscendingOrder() {
        CategoryJpaEntity category = CategoryJpaEntity.builder().name("치킨").build();
        memoryCategoryRepository.save(category);

        when(sigunguRepository.findBySigKorNm("종로구"))
            .thenReturn(Optional.of(jongno_gu()));

        for (int i = 0; i < 3; i++) {
            memoryShopRepository.save(ShopJpaEntity.builder()
                .name("bbq " + i + "호점")
                .latitude(37.0 + 0.1 * i)
                .longitude(127.0 + 0.1 * i)
                .deliveryFee(10000 - 1000 * i)
                .minimumOrderAmount(10000 - 1000 * i)
                .category(category)
                .sigungu(jongno_gu())
                .build());
        }

        ShopFindRequest findReq = ShopFindRequest.builder()
            .categoryName("치킨")
            .latitude(37.9)
            .longitude(127.9)
            .sigungu("종로구")
            .build();

        PageRequest pageable = PageRequest.of(0, 5, Sort.by("minimumOrderAmount").ascending());

        List<ShopFindResponse> result = shopService.findShopByCategory(findReq, pageable).getContent();

        Assertions.assertThat(result.get(0).getName()).isEqualTo("bbq 2호점");
        Assertions.assertThat(result.get(1).getName()).isEqualTo("bbq 1호점");
        Assertions.assertThat(result.get(2).getName()).isEqualTo("bbq 0호점");
    }

    @Test
    @DisplayName("Pageable 정렬 특성이 배달팁 낮은 순이면, 배달팁 오름차순으로 매장이 조회됨")
    void whenFindShopByCategoryOrderByDeliveryFee_thenReturnDeliveryFeeAscendingOrder() {
        CategoryJpaEntity category = CategoryJpaEntity.builder().name("치킨").build();
        memoryCategoryRepository.save(category);

        when(sigunguRepository.findBySigKorNm("종로구"))
            .thenReturn(Optional.of(jongno_gu()));

        for (int i = 0; i < 3; i++) {
            memoryShopRepository.save(ShopJpaEntity.builder()
                .name("bbq " + i + "호점")
                .latitude(37.0 + 0.1 * i)
                .longitude(127.0 + 0.1 * i)
                .deliveryFee(10000 - 1000 * i)
                .minimumOrderAmount(10000 - 1000 * i)
                .category(category)
                .sigungu(jongno_gu())
                .build());
        }

        ShopFindRequest findReq = ShopFindRequest.builder()
            .categoryName("치킨")
            .latitude(37.9)
            .longitude(127.9)
            .sigungu("종로구")
            .build();

        PageRequest pageable = PageRequest.of(0, 5, Sort.by("deliveryFee").ascending());

        List<ShopFindResponse> result = shopService.findShopByCategory(findReq, pageable).getContent();

        Assertions.assertThat(result.get(0).getName()).isEqualTo("bbq 2호점");
        Assertions.assertThat(result.get(1).getName()).isEqualTo("bbq 1호점");
        Assertions.assertThat(result.get(2).getName()).isEqualTo("bbq 0호점");
    }

    @Test
    @DisplayName("Pageable 정렬 기준이 distance이면, 가까운 거리순으로 매장이 조회됨.")
    void whenFindShopByCategoryOrderByDistance_thenReturnCloseDistanceOrder() {
        CategoryJpaEntity category = CategoryJpaEntity.builder().name("치킨").build();
        memoryCategoryRepository.save(category);

        when(sigunguRepository.findBySigKorNm("종로구"))
            .thenReturn(Optional.of(jongno_gu()));

        for (int i = 0; i < 3; i++) {
            memoryShopRepository.save(ShopJpaEntity.builder()
                .name("bbq " + i + "호점")
                .latitude(37.0 + 0.1 * i)
                .longitude(127.0 + 0.1 * i)
                .deliveryFee(10000 - 1000 * i)
                .minimumOrderAmount(10000 - 1000 * i)
                .category(category)
                .sigungu(jongno_gu())
                .build());
        }

        ShopFindRequest findReq = ShopFindRequest.builder()
            .categoryName("치킨")
            .latitude(37.9)
            .longitude(127.9)
            .sigungu("종로구")
            .build();

        PageRequest pageable = PageRequest.of(0, 5, Sort.by("distance").ascending());

        List<ShopFindResponse> result = shopService.findShopByCategory(findReq, pageable).getContent();

        Assertions.assertThat(result.get(0).getName()).isEqualTo("bbq 2호점");
        Assertions.assertThat(result.get(1).getName()).isEqualTo("bbq 1호점");
        Assertions.assertThat(result.get(2).getName()).isEqualTo("bbq 0호점");
    }

    @Disabled
    @Test
    @DisplayName("카테고리로 매장 목록 조회 시, 각 매장의 상호명, 배달팁, 최소주문금액 정보 표시")
    void whenFindChickenShop_thenReturnTitleAndDeliveryTipAndMinimumOrderAmount() {

    }

    @Disabled
    @Test
    @DisplayName("매장 상세 조회 시, 상호명, 최소주문 금액, 전화번호, 배달팁, 메뉴명, 메뉴 설명, 메뉴 가격 조회")
    void whenFindShop_thenReturnDetailInfo() {

    }

    @Test
    @DisplayName("매장이 존재하지 않으면, 매장 수정 실패")
    void givenWrongShop_whenUpdateShop_thenFail() {
        ShopUpdateRequest updateRequest = ShopUpdateRequest.builder().name("bbq").build();

        assertThrows(ShopNotExistsException.class, () -> {
            shopService.updateShop(updateRequest);
        });
    }

    @Test
    @DisplayName("매장이 존재하지만 카테고리 존재하지 않으면, 매장 수정 실패")
    void givenShopWithWrongCategory_whenUpdateShop_thenFail() {
        ShopJpaEntity shop = ShopJpaEntity.builder().name("bbq").build();
        memoryShopRepository.save(shop);

        assertThrows(CategoryNotExistsException.class, () -> {
            shopService.updateShop(ShopUpdateRequest.builder()
                .id(1L).name("bbq").category("치킨").build());
        });
    }

    @Test
    @DisplayName("매장과 카테고리가 존재하면, 매장 수정 성공")
    void whenUpdateShop_thenSuccess() {
        CategoryJpaEntity category = CategoryJpaEntity.builder().name("치킨").build();
        ShopJpaEntity shop = ShopJpaEntity.builder().name("bbq").build();

        memoryCategoryRepository.save(category);
        memoryShopRepository.save(shop);

        when(sidoRepository.findByCtpKorNm("서울특별시"))
            .thenReturn(Optional.of(seoul()));
        when(sigunguRepository.findBySigKorNm("종로구"))
            .thenReturn(Optional.of(jongno_gu()));

        assertDoesNotThrow(() -> {
            shopService.updateShop(ShopUpdateRequest.builder()
                .id(1L)
                .name("bbq")
                .category("치킨")
                .sido("서울특별시")
                .sigungu("종로구")
                .build());
        });
    }

    @Test
    @DisplayName("매장이 존재하지 않으면, 매장 삭제 실패")
    void givenWrongShop_whenDeleteShop_thenFail() {
        assertThrows(ShopNotExistsException.class, () -> {
            shopService.deleteShop(1L);
        });
    }

    @Test
    @DisplayName("매장이 존재하면, 매장 삭제 성공")
    void whenDeleteShop_thenSuccess() {
        ShopJpaEntity shop = ShopJpaEntity.builder().name("bbq").build();
        memoryShopRepository.save(shop);

        assertDoesNotThrow(() -> {
            shopService.deleteShop(1L);
        });
    }
}
