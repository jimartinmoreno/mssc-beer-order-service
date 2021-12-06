package guru.sfg.beer.order.service.sm.actions;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.beerorder.BeerOrderManagerImpl;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.events.ValidateOrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidateOrderAction_JMS_Template_MockTest {

    @Mock
    private BeerOrderRepository beerOrderRepository;
    @Mock
    private BeerOrderMapper beerOrderMapper;
    @Mock
    private JmsTemplate jmsTemplate;
    @Mock
    StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context;

    @InjectMocks
    private ValidateOrderAction validateOrderAction;

    @Captor
    ArgumentCaptor<String> queueCaptor;

    @Captor
    ArgumentCaptor<ValidateOrderRequest> validateOrderRequestArgumentCaptor;

    @BeforeEach
    void setUp() throws Exception {
        UUID uuid = UUID.randomUUID();
        Optional<BeerOrder> beerOrderOptional = Optional.of(BeerOrder.builder().id(uuid).build());
        when(context.getMessageHeader(BeerOrderManagerImpl.ORDER_ID_HEADER)).thenReturn(uuid.toString());
        when(beerOrderRepository.findById(UUID.fromString(uuid.toString()))).thenReturn(beerOrderOptional);
        when(beerOrderMapper.beerOrderToDto(any())).thenReturn(BeerOrderDto.builder().id(uuid).build());
    }

    @Test
    void execute() {
        validateOrderAction.execute(context);
        verify(jmsTemplate, times(1)).convertAndSend(queueCaptor.capture(), validateOrderRequestArgumentCaptor.capture());
        then(jmsTemplate).should().convertAndSend(anyString(), any(ValidateOrderRequest.class));

        String queue = queueCaptor.getValue();
        ValidateOrderRequest validateOrderRequest = validateOrderRequestArgumentCaptor.getValue();
        System.out.println("queue = " + queue);
        System.out.println("validateOrderRequest = " + validateOrderRequest);
    }
}