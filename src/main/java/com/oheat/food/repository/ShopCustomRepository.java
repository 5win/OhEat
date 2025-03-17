package com.oheat.food.repository;

import com.oheat.food.dto.Coordinates;
import com.oheat.food.entity.CategoryJpaEntity;
import com.oheat.food.entity.ShopJpaEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShopCustomRepository {

    Page<ShopJpaEntity> findByCategory(CategoryJpaEntity category, List<Integer> adjs, Pageable pageable);

    Page<ShopJpaEntity> findByCategoryOrderByDistance(CategoryJpaEntity category, Coordinates coordinates,
        Pageable pageable);
}
