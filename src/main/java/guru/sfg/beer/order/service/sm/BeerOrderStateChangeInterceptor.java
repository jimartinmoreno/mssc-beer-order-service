package guru.sfg.beer.order.service.sm;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.beerorder.BeerOrderManagerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by jt on 11/30/19.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BeerOrderStateChangeInterceptor extends StateMachineInterceptorAdapter<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository beerOrderRepository;

    @Transactional
    @Override
    public void preStateChange(State<BeerOrderStatusEnum, BeerOrderEventEnum> state,
                               Message<BeerOrderEventEnum> message,
                               Transition<BeerOrderStatusEnum, BeerOrderEventEnum> transition,
                               StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine,
                               StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> rootStateMachine) {
        log.debug("preStateChange - Pre-State Change");

        Optional.ofNullable(message)
                .flatMap(msg ->
                        Optional.ofNullable((String) msg.getHeaders()
                                .getOrDefault(BeerOrderManagerImpl.ORDER_ID_HEADER, " "))
                )
                .ifPresent(orderId -> {
                            log.debug("preStateChange - Saving state for order id: " + orderId + " Status: " + state.getId());

                            BeerOrder beerOrder = beerOrderRepository.getById(UUID.fromString(orderId));
                            beerOrder.setOrderStatus(state.getId());
                            beerOrder.setOrderStatusCallbackUrl("http://localhost:8080/api/v1/customers/" + beerOrder.getCustomer().getId() + "/orders/" + beerOrder.getId());
                            BeerOrder savedbeerOrder = beerOrderRepository.saveAndFlush(beerOrder); //Forzamos a hibenate que guarde directamente en BD
                            log.debug("preStateChange - Saved order: " + savedbeerOrder);
                        }
                );
    }


    /**
     * El resto de métodos que he sobreescrito es para mostrar las posibles opciones
     */

    @Override
    public Message<BeerOrderEventEnum> preEvent(Message<BeerOrderEventEnum> message,
                                                StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine) {
        return super.preEvent(message, stateMachine);
    }

    @Transactional
    @Override
    public void postStateChange(State<BeerOrderStatusEnum, BeerOrderEventEnum> state,
                                Message<BeerOrderEventEnum> message,
                                Transition<BeerOrderStatusEnum, BeerOrderEventEnum> transition,
                                StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine,
                                StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> rootStateMachine) {
        Optional.ofNullable(message)
                .flatMap(msg ->
                        Optional.ofNullable((String) msg.getHeaders()
                                .getOrDefault(BeerOrderManagerImpl.ORDER_ID_HEADER, " "))
                )
                .ifPresent(orderId -> {

                            BeerOrder beerOrder = beerOrderRepository.getById(UUID.fromString(orderId));
                            beerOrder.setOrderStatus(state.getId());

                            log.debug("postStateChange - saved state for order id: " + beerOrder.getId() + " Status: " + beerOrder.getOrderStatus());
                        }
                );
    }

    @Override
    public StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> preTransition(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
        return super.preTransition(stateContext);
    }

    @Override
    public StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> postTransition(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
        return super.postTransition(stateContext);
    }

    @Override
    public Exception stateMachineError(StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine,
                                       Exception exception) {
        return super.stateMachineError(stateMachine, exception);
    }
}
