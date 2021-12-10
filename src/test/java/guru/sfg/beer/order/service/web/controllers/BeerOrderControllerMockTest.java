package guru.sfg.beer.order.service.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.sfg.beer.order.service.bootstrap.BeerOrderBootStrap;
import guru.sfg.beer.order.service.services.beerorder.BeerOrderService;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.BeerOrderLineDto;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@SpringBootTest
@AutoConfigureJsonTesters
class BeerOrderControllerMockTest {

    @Autowired
    BeerOrderController beerOrderController;

    /**
     * @MockBean Annotation that can be used to add mocks to a Spring ApplicationContext. Can be used as a class level annotation
     * or on fields in either @Configuration classes, or test classes that are @RunWith the SpringRunner.
     */
    @MockBean
    BeerOrderService beerOrderService;

    @Autowired
    private ObjectMapper objectMapper;

    private BeerOrderDto beerOrderDto;

    @Captor
    private ArgumentCaptor<BeerOrderDto> beerOrderDtoArgumentCaptor;

    @Captor
    private ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Autowired
    private JacksonTester<BeerOrderDto> beerOrderDtoJacksonTester;

    @BeforeEach
    void setUp() {
        beerOrderDto = getBeerOrderDto();
        given(beerOrderService.placeOrder(any(UUID.class), any(BeerOrderDto.class))).willReturn(beerOrderDto);
    }

    @Test
    void placeOrder() throws Exception {
        BeerOrderDto result = beerOrderController.placeOrder(UUID.randomUUID(), beerOrderDto);

        System.out.println("result = " + result);

        verify(beerOrderService, times(1)).placeOrder(uuidArgumentCaptor.capture(), beerOrderDtoArgumentCaptor.capture());
        assertThat(result).isNotNull();
        assertThat(uuidArgumentCaptor.getValue()).isNotNull();
        assertThat(beerOrderDto).isEqualTo(beerOrderDtoArgumentCaptor.getValue());

        JsonContent<BeerOrderDto> jsonContent = this.beerOrderDtoJacksonTester.write(objectMapper.readValue(objectMapper.writeValueAsString(result), BeerOrderDto.class));

        AssertionsForInterfaceTypes.assertThat(jsonContent)
                .extractingJsonPathStringValue("@.customerRef").isNotNull();
        AssertionsForInterfaceTypes.assertThat(jsonContent)
                .extractingJsonPathStringValue("@.customerId").isNotNull();
    }

    private BeerOrderDto getBeerOrderDto() {
        String beerToOrder = BeerOrderBootStrap.BEER_1_UPC;

        BeerOrderLineDto beerOrderLine = BeerOrderLineDto.builder()
                .upc(beerToOrder)
                .orderQuantity(new Random().nextInt(6))
                .beerId(UUID.randomUUID())
                .quantityAllocated(new Random().nextInt(30))
                .build();

        List<BeerOrderLineDto> beerOrderLineSet = new ArrayList<>();
        beerOrderLineSet.add(beerOrderLine);

        BeerOrderDto beerOrder = BeerOrderDto.builder()
                .customerId(UUID.randomUUID())
                .customerRef(UUID.randomUUID().toString())
                .beerOrderLines(beerOrderLineSet)
                .build();
        return beerOrder;
    }
}