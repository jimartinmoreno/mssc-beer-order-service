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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {BeerOrderController.class})
@AutoConfigureJsonTesters
class BeerOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * @MockBean Annotation that can be used to add mocks to a Spring ApplicationContext. Can be used as a class level annotation
     * or on fields in either @Configuration classes, or test classes that are @RunWith the SpringRunner.
     */
    @MockBean
    private BeerOrderService beerOrderService;

    @Captor
    private ArgumentCaptor<BeerOrderDto> beerOrderDtoArgumentCaptor;

    @Captor
    private ArgumentCaptor<UUID> uuidArgumentCaptor;

    private BeerOrderDto beerOrderDto;

    @Autowired
    private JacksonTester<BeerOrderDto> beerOrderDtoJacksonTester;

    @BeforeEach
    void setUp() {
        beerOrderDto = getBeerOrderDto();
        given(beerOrderService.placeOrder(any(UUID.class), any(BeerOrderDto.class))).willReturn(beerOrderDto);
    }

    @Test
    void placeOrder() throws Exception {

        String requestJson = objectMapper.writeValueAsString(beerOrderDto);


        MvcResult result = mockMvc.perform(post("/api/v1/customers/{customerId}/orders", UUID.randomUUID().toString())
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        System.out.println("result.getResponse().getContentAsString() = " + responseString);
        System.out.println("result.getResponse().getContentType() = " + result.getResponse().getContentType());
        System.out.println("result.getResponse().getStatus() = " + result.getResponse().getStatus());

        verify(beerOrderService, times(1)).placeOrder(uuidArgumentCaptor.capture(), beerOrderDtoArgumentCaptor.capture());
        assertThat(uuidArgumentCaptor.getValue()).isNotNull();
        assertThat(beerOrderDto).isEqualTo(beerOrderDtoArgumentCaptor.getValue());
        assertThat(requestJson).isEqualTo(result.getResponse().getContentAsString());

        JsonContent<BeerOrderDto> jsonContent = this.beerOrderDtoJacksonTester.write(objectMapper.readValue(responseString, BeerOrderDto.class));

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