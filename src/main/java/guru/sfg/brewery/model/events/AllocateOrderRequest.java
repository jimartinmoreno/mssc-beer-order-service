package guru.sfg.brewery.model.events;

import guru.sfg.brewery.model.BeerOrderDto;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AllocateOrderRequest {
    private BeerOrderDto beerOrderDto;
}
