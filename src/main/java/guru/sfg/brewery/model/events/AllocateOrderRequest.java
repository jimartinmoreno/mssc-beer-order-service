package guru.sfg.brewery.model.events;

import guru.sfg.brewery.model.BeerOrderDto;
import lombok.*;

/**
 * Created by jt on 12/2/19.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AllocateOrderRequest {
    private BeerOrderDto beerOrderDto;
}
