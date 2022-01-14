package guru.sfg.beer.order.service.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.sfg.beer.order.service.services.customer.CustomerService;
import guru.sfg.brewery.model.CustomerDto;
import guru.sfg.brewery.model.CustomerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {CustomerController.class})
class CustomerControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * @MockBean Annotation that can be used to add mocks to a Spring ApplicationContext. Can be used as a class level annotation
     * or on fields in either @Configuration classes, or test classes that are @RunWith the SpringRunner.
     */
    @MockBean
    private CustomerService customerService;


    @Captor
    private ArgumentCaptor<PageRequest> pageRequestArgumentCaptor;


    @BeforeEach
    void setUp() {
    }

    @Test
    void listCustomersTest() throws Exception {
        given(customerService.listCustomers(any(Pageable.class))).willReturn(getCustomers());
        MvcResult result = mockMvc.perform(get("/api/v1/customers")
                        .queryParam("pageNumber", "1")
                        .queryParam("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        String responseString = result.getResponse().getContentAsString();
        System.out.println("result.getResponse().getContentAsString() = " + responseString);
        System.out.println("result.getResponse().getContentType() = " + result.getResponse().getContentType());
        System.out.println("result.getResponse().getStatus() = " + result.getResponse().getStatus());


        verify(customerService, times(1)).listCustomers(pageRequestArgumentCaptor.capture());
        assertThat(pageRequestArgumentCaptor.getValue()).isNotNull();
        assertThat(PageRequest.of(1, 10)).isEqualTo(pageRequestArgumentCaptor.getValue());
    }

    CustomerPagedList getCustomers() {
        List<CustomerDto> content = new ArrayList<>();
        content.add( CustomerDto.builder()
                .id(UUID.randomUUID())
                .customerName("Nacho")
                .version(1)
                .build());

        return new CustomerPagedList(content,
                PageRequest.of(1, 10),
               10);
    }
}