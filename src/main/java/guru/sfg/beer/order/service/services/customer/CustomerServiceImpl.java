package guru.sfg.beer.order.service.services.customer;

import guru.sfg.beer.order.service.domain.Customer;
import guru.sfg.beer.order.service.repositories.CustomerRepository;
import guru.sfg.beer.order.service.web.mappers.CustomerMapper;
import guru.sfg.brewery.model.CustomerDto;
import guru.sfg.brewery.model.CustomerPagedList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerPagedList listCustomers(Pageable pageable) {
        Page<Customer> customerPage = customerRepository.findAll(pageable);

        return new CustomerPagedList(customerPage.stream().map(customerMapper::customerToDto).toList(),
                // PageRequest.of(customerPage.getPageable().getPageNumber(), customerPage.getPageable().getPageSize()),
                customerPage.getPageable(),
                customerPage.getTotalElements());
    }

    @Override
    public List<CustomerDto> listCustomers() {
        return customerRepository.findAll().stream().map(customerMapper::customerToDto).toList();
    }
}
