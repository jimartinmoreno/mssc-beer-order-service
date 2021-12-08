package guru.sfg.beer.order.service.services.beer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import guru.sfg.brewery.model.BeerDto;
import guru.sfg.brewery.model.BeerPagedList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@ExtendWith(WireMockExtension.class)
@SpringBootTest
class BeerServiceImplWireMockTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WireMockServer wireMockServer;

    @Autowired
    BeerService beerService;

    BeerDto beerDto;

    UUID beerId = UUID.randomUUID();

    /**
     * @Configuration that can be used to define additional beans or customizations for a test.
     * Unlike regular @Configuration classes the use of @TestConfiguration does not prevent auto-detection
     * of @SpringBootConfiguration.
     */
    @TestConfiguration
    static class RestTemplateBuilderProvider {
        /**
         * destroyMethod = "stop" > The optional name of a method to call on the bean instance upon closing the application
         * context, for example a close() method on a JDBC DataSource implementation, or a Hibernate SessionFactory object.
         * The method must have no arguments but may throw any exception.
         */
        @Bean(destroyMethod = "stop")
        public WireMockServer wireMockServer() {
            // Esta configuracion a nivel de puerto coincide con lo configurado en el application.properties de /test
            WireMockServer server = with(wireMockConfig().port(1234));
            //WireMockServer server = new WireMockServer();
            server.start();
            return server;
        }
    }

    @BeforeEach
    void setUp() throws JsonProcessingException {
        beerDto = BeerDto.builder().id(beerId).upc("0631234300019").build();
        BeerPagedList beerPagedList = new BeerPagedList(List.<BeerDto>of(beerDto));
        wireMockServer.stubFor(get(BeerServiceImpl.BEER_PATH_V1)
                .willReturn(okJson(objectMapper.writeValueAsString(beerPagedList))));
    }

    @AfterEach
    void tearDown() {
        wireMockServer.resetAll();
        wireMockServer.removeStub(get(BeerServiceImpl.BEER_PATH_V1));
//        wireMockServer.stop();
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