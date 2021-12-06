package guru.sfg.beer.order.service.services.beer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.sfg.brewery.model.BeerDto;
import guru.sfg.brewery.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(BeerServiceImpl.class)
@AutoConfigureJsonTesters
class BeerServiceImplRestClientTest {

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    BeerService beerService;

    BeerDto beerDto;

    UUID beerId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws JsonProcessingException {
        beerDto = BeerDto.builder().id(beerId).upc("0631234300019").build();
        BeerPagedList beerPagedList = new BeerPagedList(List.<BeerDto>of(beerDto));

        String detailsString = objectMapper.writeValueAsString(beerPagedList);
        this.mockServer.expect(requestToUriTemplate("http://localhost:1234/" + BeerServiceImpl.BEER_PATH_V1))
                .andRespond(withSuccess(detailsString, MediaType.APPLICATION_JSON));
    }

    @Test
    void getBeersTest() throws JsonProcessingException {
        List<BeerDto> beerDtos = beerService.getBeers();
        System.out.println("beerDtos = " + beerDtos);
    }

    @Test
    void getBeerById() {
    }

    @Test
    void getBeerByUpc() {
    }
}