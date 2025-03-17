package com.oheat.order.entity;

import com.oheat.common.BaseTimeEntity;
import com.oheat.food.entity.OptionJpaEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Entity
@Table(name = "orders_options")
public class OrderOption extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "options_id", nullable = false)
    private OptionJpaEntity option;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_options_group_id", nullable = false)
    private OrderOptionGroup orderOptionGroup;

    @Builder
    public OrderOption(OptionJpaEntity option, OrderOptionGroup orderOptionGroup) {
        this.option = option;
        this.orderOptionGroup = orderOptionGroup;
    }

    public void setOrderOptionGroup(OrderOptionGroup orderOptionGroup) {
        this.orderOptionGroup = orderOptionGroup;
    }
}
