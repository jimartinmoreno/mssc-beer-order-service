package guru.sfg.beer.order.service.services.customer;

import guru.sfg.brewery.model.CustomerPagedList;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;


//@ActiveProfiles(value = {"localmysql"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackages = {"guru.sfg.beer.order.service.services.customer",
        "guru.sfg.beer.order.service.services.beer",
        "guru.sfg.beer.order.service.bootstrap",
        "guru.sfg.beer.order.service.web.mappers"})
@DataJpaTest(showSql = true)
@AutoConfigureWebClient
@AutoConfigureMockRestServiceServer
@Slf4j
class CustomerServiceImplIT {

    @Autowired
    CustomerService customerService;

       @Test
    void listCustomers() {
        log.info("customerService: " + customerService);
        CustomerPagedList customerPagedList = customerService.listCustomers(PageRequest.of(0, 25));
        log.info("customerPagedList: " + customerPagedList.toList());
        assertThat(customerPagedList).isNotNull();
        assertThat(customerPagedList.getTotalElements()).isPositive();
        assertThat(customerPagedList.getSize()).isPositive();
        assertThat(customerPagedList.toList().size()).isPositive();
    }
}