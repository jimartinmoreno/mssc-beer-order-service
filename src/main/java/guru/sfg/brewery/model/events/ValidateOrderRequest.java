package guru.sfg.brewery.model.events;

import guru.sfg.brewery.model.BeerOrderDto;
import lombok.*;

/**
 * Created by jt on 11/30/19.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ValidateOrderRequest {

    private BeerOrderDto beerOrder;
}
