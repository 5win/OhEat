package com.oheat.user;

import static com.oheat.common.SidogunguFixture.jongno_gu;
import static com.oheat.common.SidogunguFixture.seoul;
import static org.assertj.core.api.Assertions.assertThat;

import com.oheat.common.sigungu.SigunguJpaRepository;
import com.oheat.food.entity.CategoryJpaEntity;
import com.oheat.food.entity.MenuJpaEntity;
import com.oheat.food.entity.OptionGroupJpaEntity;
import com.oheat.food.entity.OptionJpaEntity;
import com.oheat.food.entity.ShopJpaEntity;
import com.oheat.food.repository.CategoryJpaRepository;
import com.oheat.food.repository.MenuJpaRepository;
import com.oheat.food.repository.OptionGroupJpaRepository;
import com.oheat.food.repository.OptionJpaRepository;
import com.oheat.food.repository.ShopJpaRepository;
import com.oheat.user.constant.Role;
import com.oheat.user.dto.CartSaveRequest;
import com.oheat.user.dto.CartSaveRequest.CartOptionGroupSaveRequest;
import com.oheat.user.entity.CartJpaEntity;
import com.oheat.user.entity.CartOptionGroup;
import com.oheat.user.entity.CartOptionGroupOption;
import com.oheat.user.entity.UserJpaEntity;
import com.oheat.user.repository.CartJpaRepository;
import com.oheat.user.repository.UserJpaRepository;
import com.oheat.user.service.CartService;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class CartIntegrationTest {

    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private CategoryJpaRepository categoryJpaRepository;
    @Autowired
    private ShopJpaRepository shopJpaRepository;
    @Autowired
    private MenuJpaRepository menuJpaRepository;
    @Autowired
    private OptionGroupJpaRepository optionGroupJpaRepository;
    @Autowired
    private OptionJpaRepository optionJpaRepository;
    @Autowired
    private CartJpaRepository cartJpaRepository;
    @Autowired
    private SigunguJpaRepository sigunguJpaRepository;
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CartService cartService;

    @BeforeEach
    void reset() {
        entityManager.createNativeQuery("ALTER TABLE shop AUTO_INCREMENT=1")
            .executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE menu AUTO_INCREMENT=1")
            .executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE option_group AUTO_INCREMENT=1")
            .executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE options AUTO_INCREMENT=1")
            .executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE cart AUTO_INCREMENT=1")
            .executeUpdate();
    }

    @Test
    @DisplayName("옵션그룹과 옵션이 모두 같은 메뉴가 이미 추가되어 있으면, 기존 정보에서 개수만 증가시킨다")
    void givenSameCartItem_whenAddToCart_thenIncreaseAmount() {

        setupData();

        // 새로 장바구니에 담는 작업 시작
        CartSaveRequest saveRequest = CartSaveRequest.builder()
            .shopId(1L)
            .menuId(1L)
            .amount(2)
            .optionGroups(
                List.of(CartOptionGroupSaveRequest.builder()
                        .optionGroupId(1L)
                        .options(List.of(1L))
                        .build(),
                    CartOptionGroupSaveRequest.builder()
                        .optionGroupId(2L)
                        .options(List.of(2L))
                        .build())
            ).build();

        Assertions.assertDoesNotThrow(() -> {
            cartService.registerCart("username", saveRequest);
        });

        CartJpaEntity result = cartJpaRepository.findById(1L).get();
        assertThat(result.getAmount()).isEqualTo(3);
    }

    @Test
    @DisplayName("장바구니에 이미 담겨있는 메뉴지만 옵션 그룹 개수가 다르면, 새로 장바구니에 추가한다.")
    void givenDifferentOptionGroupSize_whenAddToCart_thenRegisterNewCartItem() {

        setupData();

        // 새로 장바구니에 담는 작업 시작
        // 두 번째 옵션 그룹의 옵션이 1개 다르므로, 별도로 저장되어야 함
        CartSaveRequest saveRequest = CartSaveRequest.builder()
            .shopId(1L)
            .menuId(1L)
            .amount(2)
            .optionGroups(
                List.of(CartOptionGroupSaveRequest.builder()
                    .optionGroupId(1L)
                    .options(List.of(1L))
                    .build())
            ).build();

        Assertions.assertDoesNotThrow(() -> {
            cartService.registerCart("username", saveRequest);
        });

        CartJpaEntity result1 = cartJpaRepository.findById(1L).get();
        CartJpaEntity result2 = cartJpaRepository.findById(2L).get();
        assertThat(result1.getAmount()).isEqualTo(1);
        assertThat(result2.getAmount()).isEqualTo(2);
    }

    @Test
    @DisplayName("장바구니에 이미 담겨있는 메뉴이고 옵션 그룹 개수가 같지만 옵션 그룹이 하나라도 다르면, 새로 장바구니에 추가한다")
    void givenDifferentOptionGroup_whenAddToCart_thenRegisterNewCartItem() {

        setupData();

        // 새로 장바구니에 담는 작업 시작
        // 두 번째 옵션 그룹의 옵션이 1개 다르므로, 별도로 저장되어야 함
        CartSaveRequest saveRequest = CartSaveRequest.builder()
            .shopId(1L)
            .menuId(1L)
            .amount(2)
            .optionGroups(
                List.of(CartOptionGroupSaveRequest.builder()
                        .optionGroupId(1L)
                        .options(List.of(1L))
                        .build(),
                    CartOptionGroupSaveRequest.builder()
                        .optionGroupId(3L)                  // 기존 장바구니 항목과 다른 옵션 그룹
                        .options(List.of(4L))
                        .build())
            ).build();

        Assertions.assertDoesNotThrow(() -> {
            cartService.registerCart("username", saveRequest);
        });

        CartJpaEntity result1 = cartJpaRepository.findById(1L).get();
        CartJpaEntity result2 = cartJpaRepository.findById(2L).get();
        assertThat(result1.getAmount()).isEqualTo(1);
        assertThat(result2.getAmount()).isEqualTo(2);
    }

    @Test
    @DisplayName("장바구니에 이미 담겨있는 메뉴지만 옵션 개수가 다르면, 새로 장바구니에 추가한다")
    void givenDifferentOptionSize_whenAddToCart_thenRegisterNewCartItem() {

        setupData();

        // 새로 장바구니에 담는 작업 시작
        // 두 번째 옵션 그룹의 옵션이 1개 다르므로, 별도로 저장되어야 함
        CartSaveRequest saveRequest = CartSaveRequest.builder()
            .shopId(1L)
            .menuId(1L)
            .amount(2)
            .optionGroups(
                List.of(CartOptionGroupSaveRequest.builder()
                        .optionGroupId(1L)
                        .options(List.of(1L))
                        .build(),
                    CartOptionGroupSaveRequest.builder()
                        .optionGroupId(2L)                  // 기존 장바구니 항목과 다른 옵션 그룹
                        .options(List.of(2L, 3L))
                        .build())
            ).build();

        Assertions.assertDoesNotThrow(() -> {
            cartService.registerCart("username", saveRequest);
        });

        CartJpaEntity result1 = cartJpaRepository.findById(1L).get();
        CartJpaEntity result2 = cartJpaRepository.findById(2L).get();
        assertThat(result1.getAmount()).isEqualTo(1);
        assertThat(result2.getAmount()).isEqualTo(2);
    }

    @Test
    @DisplayName("장바구니에 이미 담겨있는 메뉴이고 옵션 개수가 같지만 옵션이 하나라도 다르면, 새로 장바구니에 추가한다")
    void givenDifferentOption_whenAddToCart_thenRegisterNewCartItem() {

        setupData();

        // 새로 장바구니에 담는 작업 시작
        // 두 번째 옵션 그룹의 옵션이 1개 다르므로, 별도로 저장되어야 함
        CartSaveRequest saveRequest = CartSaveRequest.builder()
            .shopId(1L)
            .menuId(1L)
            .amount(2)
            .optionGroups(
                List.of(CartOptionGroupSaveRequest.builder()
                        .optionGroupId(1L)
                        .options(List.of(1L))
                        .build(),
                    CartOptionGroupSaveRequest.builder()
                        .optionGroupId(2L)                  // 기존 장바구니 항목과 다른 옵션 그룹
                        .options(List.of(3L))
                        .build())
            ).build();

        Assertions.assertDoesNotThrow(() -> {
            cartService.registerCart("username", saveRequest);
        });

        CartJpaEntity result1 = cartJpaRepository.findById(1L).get();
        CartJpaEntity result2 = cartJpaRepository.findById(2L).get();
        assertThat(result1.getAmount()).isEqualTo(1);
        assertThat(result2.getAmount()).isEqualTo(2);
    }

    @Disabled
    @Test
    @DisplayName("매장, 메뉴, 옵션그룹, 옵션 중에 하나라도 삭제되면, 해당 정보를 담은 메뉴가 장바구니에서 삭제된다")
    void test16() {

    }

    private void setupData() {
        // 유저, 매장, 메뉴 등등의 정보를 미리 저장
        UserJpaEntity user = UserJpaEntity.builder()
            .username("username")
            .password("pw")
            .phone("010-1234-1234")
            .role(Role.CUSTOMER)
            .build();
        CategoryJpaEntity category = CategoryJpaEntity.builder()
            .name("치킨")
            .build();
        ShopJpaEntity shop = ShopJpaEntity.builder()
            .name("bbq")
            .minimumOrderAmount(10000)
            .deliveryFee(2000)
            .category(category)
            .address("서울특별시 종로구")
            .latitude(37.0)
            .longitude(127.0)
            .sido(seoul())
            .sigungu(jongno_gu())
            .build();
        MenuJpaEntity menu = MenuJpaEntity.builder()
            .name("황올")
            .price(20000)
            .shop(shop)
            .build();
        OptionGroupJpaEntity optionGroup1 = OptionGroupJpaEntity.builder()
            .name("부분육 선택")
            .menu(menu)
            .build();
        OptionGroupJpaEntity optionGroup2 = OptionGroupJpaEntity.builder()
            .name("음료 선택")
            .menu(menu)
            .build();
        OptionGroupJpaEntity optionGroup3 = OptionGroupJpaEntity.builder()
            .name("소스 추가")
            .menu(menu)
            .build();
        OptionJpaEntity option1 = OptionJpaEntity.builder()
            .name("순살")
            .optionGroup(optionGroup1)
            .price(1000)
            .build();
        OptionJpaEntity option2 = OptionJpaEntity.builder()
            .name("콜라")
            .optionGroup(optionGroup2)
            .price(2000)
            .build();
        OptionJpaEntity option3 = OptionJpaEntity.builder()
            .name("사이다")
            .optionGroup(optionGroup2)
            .price(3000)
            .build();
        OptionJpaEntity option4 = OptionJpaEntity.builder()
            .name("양념 소스")
            .optionGroup(optionGroup3)
            .price(500)
            .build();

        userJpaRepository.save(user);
        categoryJpaRepository.save(category);
        sigunguJpaRepository.save(jongno_gu());
        shopJpaRepository.save(shop);
        menuJpaRepository.save(menu);
        optionGroupJpaRepository.save(optionGroup1);
        optionGroupJpaRepository.save(optionGroup2);
        optionGroupJpaRepository.save(optionGroup3);
        optionJpaRepository.save(option1);
        optionJpaRepository.save(option2);
        optionJpaRepository.save(option3);
        optionJpaRepository.save(option4);

        // 장바구니에 미리 아이템 추가
        // 총 옵션 그룹은 2개이며, 각 그룹에서 1개와 2개의 옵션이 선택됨

        // 옵션그룹1
        CartJpaEntity cart = CartJpaEntity.builder()
            .amount(1)
            .user(user)
            .shop(shop)
            .menu(menu)
            .build();
        CartOptionGroup cartOptionGroup1 = CartOptionGroup.builder()
            .cart(cart)
            .optionGroup(optionGroup1)
            .build();
        CartOptionGroupOption cartOptionGroupOption1 = CartOptionGroupOption.builder()
            .cartOptionGroup(cartOptionGroup1)
            .option(option1)
            .build();
        cartOptionGroup1.addCartOption(cartOptionGroupOption1);
        cart.addCartOptionGroup(cartOptionGroup1);

        // 옵션그룹2
        CartOptionGroup cartOptionGroup2 = CartOptionGroup.builder()
            .cart(cart)
            .optionGroup(optionGroup2)
            .build();
        CartOptionGroupOption cartOptionGroupOption2 = CartOptionGroupOption.builder()
            .cartOptionGroup(cartOptionGroup2)
            .option(option2)
            .build();
        cartOptionGroup2.addCartOption(cartOptionGroupOption2);
        cart.addCartOptionGroup(cartOptionGroup2);

        // 장바구니에 담음
        cartJpaRepository.save(cart);
    }
}
