package guru.sfg.beer.order.service.services.beer;

import guru.sfg.brewery.model.BeerDto;
import guru.sfg.brewery.model.BeerPagedList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by jt on 2019-06-09.
 */
@ConfigurationProperties(prefix = "sfg.brewery", ignoreUnknownFields = false)
@Service
@Slf4j
public class BeerServiceImpl implements BeerService {
    public final static String BEER_PATH_V1 = "/api/v1/beer/";
    public final static String BEER_UPC_PATH_V1 = "/api/v1/beerUpc/";
    private final RestTemplate restTemplate;

    private String beerServiceHost;

    public BeerServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public Optional<BeerDto> getBeerById(UUID uuid){
        return Optional.of(restTemplate.getForObject(beerServiceHost + BEER_PATH_V1 + uuid.toString(), BeerDto.class));
    }

    @Override
    public Optional<BeerDto> getBeerByUpc(String upc) {
        return Optional.of(restTemplate.getForObject(beerServiceHost + BEER_UPC_PATH_V1 + upc, BeerDto.class));
    }

    @Override
    public List<BeerDto> getBeers() {
        ParameterizedTypeReference<BeerPagedList> responseType = new ParameterizedTypeReference<BeerPagedList>() { };
        ResponseEntity<BeerPagedList> result = restTemplate
                .exchange(beerServiceHost + BEER_PATH_V1, HttpMethod.GET,
                        null, responseType);
        List<BeerDto> searchResult = result.getBody().getContent();
        log.debug("searchResult: " + result.getBody().getContent());
        log.debug("searchResult: " + result.getBody().stream().toList());
        return searchResult;
    }

    public void setBeerServiceHost(String beerServiceHost) {
        this.beerServiceHost = beerServiceHost;
    }
}
