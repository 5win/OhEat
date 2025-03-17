package com.oheat.food.repository;

import com.oheat.food.dto.Coordinates;
import com.oheat.food.entity.CategoryJpaEntity;
import com.oheat.food.entity.ShopJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShopRepository {

    void save(ShopJpaEntity shopJpaEntity);

    Optional<ShopJpaEntity> findById(Long id);

    Optional<ShopJpaEntity> findByName(String name);

    Page<ShopJpaEntity> findByCategory(CategoryJpaEntity category, List<Integer> adjs, Pageable pageable);

    Page<ShopJpaEntity> findByCategoryOrderByDistance(CategoryJpaEntity category, Coordinates coordinates,
        Pageable pageable);

    void deleteById(Long shopId);
}
