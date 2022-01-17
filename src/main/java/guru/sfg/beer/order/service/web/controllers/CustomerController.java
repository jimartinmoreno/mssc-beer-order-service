package guru.sfg.beer.order.service.web.controllers;

import guru.sfg.beer.order.service.services.customer.CustomerService;
import guru.sfg.brewery.model.CustomerDto;
import guru.sfg.brewery.model.CustomerPagedList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers")
@RestController
public class CustomerController {

    private static final Integer DEFAULT_PAGE_NUMBER = 0;
    private static final Integer DEFAULT_PAGE_SIZE = 25;

    private final CustomerService customerService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public CustomerPagedList listCustomers(@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                           @RequestParam(value = "pageSize", required = false) Integer pageSize){

        System.out.println("pageNumber = " + pageNumber);
        System.out.println("pageSize = " + pageSize);

        if (pageNumber == null || pageNumber < 0){
            pageNumber = DEFAULT_PAGE_NUMBER;
        }

        if (pageSize == null || pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        return customerService.listCustomers(PageRequest.of(pageNumber, pageSize));
    }

    @GetMapping(path = "/list")
    @ResponseStatus(HttpStatus.OK)
    public List<CustomerDto> listCustomers2(@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                            @RequestParam(value = "pageSize", required = false) Integer pageSize){

        return listCustomers(pageNumber, pageSize).stream().toList();
    }


}
