package com.oheat.food.jpaTest;

import static com.oheat.common.SidogunguFixture.jongno_gu;
import static com.oheat.common.SidogunguFixture.seoul;
import static org.assertj.core.api.Assertions.assertThat;

import com.oheat.common.TestConfig;
import com.oheat.common.sigungu.SigunguJpaRepository;
import com.oheat.food.entity.CategoryJpaEntity;
import com.oheat.food.entity.MenuGroupJpaEntity;
import com.oheat.food.entity.MenuGroupMappingJpaEntity;
import com.oheat.food.entity.MenuJpaEntity;
import com.oheat.food.entity.ShopJpaEntity;
import com.oheat.food.repository.CategoryJpaRepository;
import com.oheat.food.repository.MenuGroupJpaRepository;
import com.oheat.food.repository.MenuGroupMappingJpaRepository;
import com.oheat.food.repository.MenuJpaRepository;
import com.oheat.food.repository.ShopJpaRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@Import(TestConfig.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class MenuGroupRepositoryTest {

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;
    @Autowired
    private ShopJpaRepository shopJpaRepository;
    @Autowired
    private MenuGroupJpaRepository menuGroupJpaRepository;
    @Autowired
    private MenuGroupMappingJpaRepository menuGroupMappingJpaRepository;
    @Autowired
    private MenuJpaRepository menuJpaRepository;
    @Autowired
    private SigunguJpaRepository sigunguJpaRepository;
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void reset() {
        entityManager.createNativeQuery("ALTER TABLE shop AUTO_INCREMENT=1")
            .executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE menu AUTO_INCREMENT=1")
            .executeUpdate();
    }

    @Test
    @DisplayName("해당 매장이 없으면, 새 메뉴 그룹 추가 실패")
    void usingJpa_givenWrongShop_whenRegisterMenuGroup_thenFail() {
        CategoryJpaEntity category = CategoryJpaEntity.builder().name("치킨").build();
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

        // 매장 저장한 뒤 삭제
        shopJpaRepository.save(shop);
        shopJpaRepository.deleteAll();
        entityManager.flush();

        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            menuGroupJpaRepository.save(MenuGroupJpaEntity.builder()
                .name("후라이드").shop(shop).build());
        });
        entityManager.clear();
    }

    @Test
    @DisplayName("해당 매장이 존재하면, 새 메뉴 그룹 추가 성공")
    void usingJpa_givenShop_whenRegisterMenuGroup_thenSuccess() {
        CategoryJpaEntity category = CategoryJpaEntity.builder().name("치킨").build();
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
        shopJpaRepository.save(shop);

        Assertions.assertDoesNotThrow(() -> {
            menuGroupJpaRepository.save(MenuGroupJpaEntity.builder()
                .name("후라이드").shop(shop).build());
        });
    }

    @Test
    @DisplayName("해당 메뉴 그룹이 없으면, 메뉴 그룹에 메뉴 추가 실패")
    void usingJpa_givenWrongMenu_whenAddMenuGroup_thenFail() {
        CategoryJpaEntity category = CategoryJpaEntity.builder().name("치킨").build();
        ShopJpaEntity shop = ShopJpaEntity.builder()
            .name("bbq")
            .category(category)
            .address("서울특별시 종로구")
            .latitude(37.0)
            .longitude(127.0)
            .sido(seoul())
            .sigungu(jongno_gu())
            .build();
        MenuGroupJpaEntity menuGroup = MenuGroupJpaEntity.builder().name("후라이드").shop(shop).build();
        MenuJpaEntity menu = MenuJpaEntity.builder().name("황올").shop(shop).build();

        // 메뉴는 저장하지 않음
        categoryJpaRepository.save(category);
        sigunguJpaRepository.save(jongno_gu());
        shopJpaRepository.save(shop);
        menuGroupJpaRepository.save(menuGroup);

        // 메뉴 저장 뒤 삭제
        menuJpaRepository.save(menu);
        menuJpaRepository.deleteAll();
        entityManager.flush();

        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            menuGroupMappingJpaRepository.save(MenuGroupMappingJpaEntity.builder()
                .menuGroup(menuGroup).menu(menu).build());
        });
        entityManager.clear();
    }

    @Test
    @DisplayName("매장과 메뉴가 존재하면, 메뉴 그룹에 메뉴 추가 성공")
    void usingJpa_givenShopAndMenu_whenAddMenuGroup_thenSuccess() {
        CategoryJpaEntity category = CategoryJpaEntity.builder().name("치킨").build();
        ShopJpaEntity shop = ShopJpaEntity.builder()
            .name("bbq")
            .category(category)
            .address("서울특별시 종로구")
            .latitude(37.0)
            .longitude(127.0)
            .sido(seoul())
            .sigungu(jongno_gu())
            .build();
        MenuGroupJpaEntity menuGroup = MenuGroupJpaEntity.builder().name("후라이드").shop(shop).build();
        MenuJpaEntity menu = MenuJpaEntity.builder().name("황올").shop(shop).build();

        categoryJpaRepository.save(category);
        sigunguJpaRepository.save(jongno_gu());
        shopJpaRepository.save(shop);
        menuGroupJpaRepository.save(menuGroup);
        menuJpaRepository.save(menu);

        Assertions.assertDoesNotThrow(() -> {
            menuGroupMappingJpaRepository.save(MenuGroupMappingJpaEntity.builder()
                .menuGroup(menuGroup).menu(menu).build());
        });
    }

    @Test
    @DisplayName("매장에 메뉴 그룹이 2개 있다면, 메뉴 그룹을 전체 조회했을 때 그룹 개수가 2개여야 함")
    void usingJpa_givenTwoMenuGroup_whenFindAllMenuGroup_thenReturnTwoMenuGroup() {
        CategoryJpaEntity category = CategoryJpaEntity.builder().name("치킨").build();
        ShopJpaEntity shop = ShopJpaEntity.builder()
            .name("bbq")
            .category(category)
            .address("서울특별시 종로구")
            .latitude(37.0)
            .longitude(127.0)
            .sido(seoul())
            .sigungu(jongno_gu())
            .build();
        MenuGroupJpaEntity menuGroup1 = MenuGroupJpaEntity.builder().name("후라이").shop(shop).build();
        MenuGroupJpaEntity menuGroup2 = MenuGroupJpaEntity.builder().name("양념").shop(shop).build();

        categoryJpaRepository.save(category);
        sigunguJpaRepository.save(jongno_gu());
        shopJpaRepository.save(shop);
        menuGroupJpaRepository.save(menuGroup1);
        menuGroupJpaRepository.save(menuGroup2);
        entityManager.clear();

        List<MenuGroupJpaEntity> result = shopJpaRepository.findByName("bbq").get()
            .getMenuGroups();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("후라이드 메뉴 그룹에 치킨 메뉴가 2개 있다면, 메뉴 그룹을 통해 메뉴 2개가 조회되어야 함")
    void usingJpa_givenMenuGroupWithTwoMenu_whenFindMenuInMenuGroup_thenReturnTwoMenu() {
        CategoryJpaEntity category = CategoryJpaEntity.builder().name("치킨").build();
        ShopJpaEntity shop = ShopJpaEntity.builder()
            .name("bbq")
            .category(category)
            .address("서울특별시 종로구")
            .latitude(37.0)
            .longitude(127.0)
            .sido(seoul())
            .sigungu(jongno_gu())
            .build();
        MenuGroupJpaEntity menuGroup = MenuGroupJpaEntity.builder().name("후라이").shop(shop).build();
        MenuJpaEntity menu1 = MenuJpaEntity.builder().name("황올").shop(shop).build();
        MenuJpaEntity menu2 = MenuJpaEntity.builder().name("닭다리").shop(shop).build();
        MenuGroupMappingJpaEntity mapping1 = MenuGroupMappingJpaEntity.builder()
            .menuGroup(menuGroup).menu(menu1).build();
        MenuGroupMappingJpaEntity mapping2 = MenuGroupMappingJpaEntity.builder()
            .menuGroup(menuGroup).menu(menu2).build();

        categoryJpaRepository.save(category);
        sigunguJpaRepository.save(jongno_gu());
        shopJpaRepository.save(shop);
        menuGroupJpaRepository.save(menuGroup);
        menuJpaRepository.save(menu1);
        menuJpaRepository.save(menu2);
        menuGroupMappingJpaRepository.save(mapping1);
        menuGroupMappingJpaRepository.save(mapping2);
        entityManager.clear();

        Set<MenuGroupMappingJpaEntity> result = shopJpaRepository.findByName("bbq").get()
            .getMenuGroups()
            .getFirst().getMenuGroupMappingSet();

        assertThat(result.size()).isEqualTo(2);
    }
}
