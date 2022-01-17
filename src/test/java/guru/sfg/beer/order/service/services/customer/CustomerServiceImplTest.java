package guru.sfg.beer.order.service.services.customer;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.Customer;
import guru.sfg.beer.order.service.repositories.CustomerRepository;
import guru.sfg.beer.order.service.web.mappers.CustomerMapper;
import guru.sfg.brewery.model.CustomerDto;
import guru.sfg.brewery.model.CustomerPagedList;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;


@ExtendWith(MockitoExtension.class)
@Slf4j
class CustomerServiceImplTest {
    @Mock
    CustomerRepository customerRepository;

    @Mock
    CustomerMapper customerMapper;

    @InjectMocks
    //CustomerService customerService = new CustomerServiceImpl(customerRepository, customerMapper);
    CustomerServiceImpl customerService;

    Customer customer;
    CustomerDto customerDto;
    List<Customer> customerList;

    @BeforeEach
    void setUp() {
        //MockitoAnnotations.openMocks(this);
        //given
        customer = getCustomer();
        customerDto = getCustomerDto();
        customerList = Arrays.asList(customer);
        given(customerMapper.customerToDto(any())).willReturn(customerDto);
    }

    @Test
    void testListCustomers() {
        log.info("customerService: " + customerService);
        //given
        given(customerRepository.findAll()).willReturn(customerList);

        //when
        List<CustomerDto> customerList = customerService.listCustomers();

        //then
        log.info("customerPagedList: " + customerList);
        assertThat(customerList).isNotNull();
        assertThat(customerList.size()).isPositive();
        assertThat(customerList).contains(getCustomerDto());

        then(customerRepository).should(times(1)).findAll();
    }

    @Test
    void listCustomers() {
        log.info("customerService: " + customerService);
        //given
        final Page<Customer> page = new PageImpl<>(customerList);
        given(customerRepository.findAll(any(Pageable.class))).willReturn(page);

        //when
        CustomerPagedList customerPagedList = customerService.listCustomers(PageRequest.of(0, 25));

        //then
        log.info("customerPagedList: " + customerPagedList.toList());
        log.info("customerPagedList: " + customerPagedList.getContent());
        assertThat(customerPagedList).isNotNull();
        assertThat(customerPagedList.getTotalElements()).isPositive();
        assertThat(customerPagedList.getSize()).isPositive();
        assertThat(customerPagedList.toList().size()).isPositive();
        assertThat(customerPagedList.getContent()).contains(getCustomerDto());

        then(customerRepository).should(times(1)).findAll(any(Pageable.class));
    }

    private Customer getCustomer() {
        BeerOrder beerOrder = BeerOrder.builder()
                .id(UUID.randomUUID())
                .build();

        Customer customer = Customer.builder()
                .id(UUID.fromString("8793a217-0f78-47cc-b0e1-7f38433c7418"))
                .version(0L)
                .apiKey(UUID.randomUUID())
                .customerName("Nacho Martin")
                .beerOrders(Set.of(beerOrder))
                .build();
        return customer;
    }

    private CustomerDto getCustomerDto() {
        CustomerDto customerDto = CustomerDto.builder()
                .id(UUID.fromString("8793a217-0f78-47cc-b0e1-7f38433c7418"))
                .version(0)
                .createdDate(OffsetDateTime.now())
                .lastModifiedDate(OffsetDateTime.now())
                .customerName("Nacho Martin")
                .build();
        return customerDto;
    }


}