package cz.lubsvo.rohlik.ecomm.model;

import java.time.LocalDateTime;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class Order {

    private Long id;

    @NotNull
    private OrderStatus status;

    private LocalDateTime createdAt;

    @Size(min=1)
    @NotNull
    private Set<OrderItem> orderedItems;
}
