package com.oheat.food.repository;

import static com.oheat.common.sigungu.QSigungu.sigungu;
import static com.oheat.food.entity.QShopJpaEntity.shopJpaEntity;

import com.oheat.food.dto.Coordinates;
import com.oheat.food.entity.CategoryJpaEntity;
import com.oheat.food.entity.ShopJpaEntity;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
public class ShopCustomRepositoryImpl implements ShopCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<ShopJpaEntity> findByCategory(CategoryJpaEntity category, List<Integer> adjs, Pageable pageable) {

        OrderSpecifier[] orders = createOrderSpecifier(pageable.getSort());

        List<ShopJpaEntity> content = jpaQueryFactory
            .selectFrom(shopJpaEntity)
            .innerJoin(sigungu)
            .on(shopJpaEntity.sigungu.eq(sigungu))
            .where(shopJpaEntity.category.eq(category).and(sigungu.ogrFid.in(adjs)))
            .orderBy(orders)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = selectTotal(category);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<ShopJpaEntity> findByCategoryOrderByDistance(CategoryJpaEntity category, Coordinates coordinates,
        Pageable pageable) {

        final double EARTH_RADIUS_KM = 6371.0;      // 지구 반지름 (km)
        final Double latitude = coordinates.getLatitude();
        final Double longitude = coordinates.getLongitude();

        // 하버사인 공식 구현
        NumberTemplate<Double> distanceExpression = Expressions.numberTemplate(Double.class,
            "({0} * acos(cos(radians({1})) * cos(radians({2})) * cos(radians({3}) - radians({4})) + sin(radians({1})) * sin(radians({2}))))",
            EARTH_RADIUS_KM,
            latitude,
            shopJpaEntity.latitude,
            longitude,
            shopJpaEntity.longitude
        );

        List<ShopJpaEntity> content = jpaQueryFactory
            .select(shopJpaEntity)
            .from(shopJpaEntity)
            .where(shopJpaEntity.category.eq(category))
            .orderBy(distanceExpression.asc())
            .orderBy(shopJpaEntity.id.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = selectTotal(category);

        return new PageImpl<>(content, pageable, total);
    }

    private Long selectTotal(CategoryJpaEntity category) {
        return jpaQueryFactory
            .select(shopJpaEntity.count())
            .from(shopJpaEntity)
            .where(shopJpaEntity.category.eq(category))
            .fetchOne();
    }

    private OrderSpecifier[] createOrderSpecifier(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        for (Sort.Order order : sort) {
            Order direction = order.getDirection().isDescending() ? Order.DESC : Order.ASC;
            String property = order.getProperty();
            switch (property) {
                case "id":
                    orders.add(new OrderSpecifier(direction, shopJpaEntity.id));
                case "minimumOrderAmount":
                    orders.add(new OrderSpecifier(direction, shopJpaEntity.minimumOrderAmount));
                case "deliveryFee":
                    orders.add(new OrderSpecifier(direction, shopJpaEntity.deliveryFee));
            }
        }
        return orders.toArray(OrderSpecifier[]::new);
    }
}
