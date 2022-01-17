package guru.sfg.beer.order.service.services.customer;

import guru.sfg.brewery.model.CustomerDto;
import guru.sfg.brewery.model.CustomerPagedList;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface CustomerService {
    CustomerPagedList listCustomers(Pageable pageable);
    List<CustomerDto> listCustomers();
}
