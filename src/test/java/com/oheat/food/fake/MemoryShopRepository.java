package com.oheat.food.fake;

import com.oheat.food.dto.Coordinates;
import com.oheat.food.entity.CategoryJpaEntity;
import com.oheat.food.entity.ShopJpaEntity;
import com.oheat.food.repository.ShopRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@Getter
public class MemoryShopRepository implements ShopRepository {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private final Map<Long, ShopJpaEntity> shops = new HashMap<>();
    private Long autoId = 1L;

    @Override
    public void save(ShopJpaEntity shopJpaEntity) {
        shops.put(autoId++, shopJpaEntity);
    }

    @Override
    public Optional<ShopJpaEntity> findById(Long id) {
        return Optional.ofNullable(shops.get(id));
    }

    @Override
    public Optional<ShopJpaEntity> findByName(String name) {
        return shops.values().stream()
            .filter(shop -> shop.getName().equals(name))
            .findFirst();
    }

    @Override
    public Page<ShopJpaEntity> findByCategory(CategoryJpaEntity category, List<Integer> adjs, Pageable pageable) {
        List<ShopJpaEntity> shopList = shops.values().stream()
            .filter(shop -> shop.getCategory().equals(category))
            .filter(shop -> adjs.contains(shop.getSigungu().getOgrFid()))
            .toList();

        if (pageable.getSort().getOrderFor("deliveryFee") != null) {
            shopList = shopList.stream()
                .sorted(Comparator.comparing(ShopJpaEntity::getDeliveryFee))
                .toList();
        }

        if (pageable.getSort().getOrderFor("minimumOrderAmount") != null) {
            shopList = shopList.stream()
                .sorted(Comparator.comparing(ShopJpaEntity::getMinimumOrderAmount))
                .toList();
        }
        return new PageImpl<>(shopList);
    }

    @Override
    public Page<ShopJpaEntity> findByCategoryOrderByDistance(CategoryJpaEntity category, Coordinates coordinates,
        Pageable pageable) {

        List<ShopJpaEntity> shopList = shops.values().stream()
            .filter(shop -> shop.getCategory().equals(category))
            .toList();

        Integer[] index = new Integer[shopList.size()];
        for (int i = 0; i < index.length; i++) {
            index[i] = i;
        }

        // 거리 계산 후, distance[i] 에 저장
        double[] distance = new double[shopList.size()];
        for (int i = 0; i < shopList.size(); i++) {
            double lat1 = coordinates.getLatitude();
            double lon1 = coordinates.getLongitude();
            double lat2 = shopList.get(i).getLatitude();
            double lon2 = shopList.get(i).getLongitude();
            distance[i] = calculateDistance(lat1, lon1, lat2, lon2);
        }
        // distance에 저장된 값을 기준으로 하여 거리순으로 인덱스 정렬
        Arrays.sort(index, Comparator.comparingInt(i -> (int) (distance[i] * 1000000)));

        // 인덱스 순서에 맞게 매장 순서 재구성
        List<ShopJpaEntity> result = new ArrayList<>();
        for (int i = 0; i < index.length; i++) {
            result.add(shopList.get(index[i]));
        }
        return new PageImpl<>(result);
    }

    @Override
    public void deleteById(Long shopId) {
        shops.remove(shopId);
    }

    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(rLat1) * Math.cos(rLat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
