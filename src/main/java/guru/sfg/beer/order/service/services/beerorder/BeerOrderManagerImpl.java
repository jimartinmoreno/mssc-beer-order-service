package guru.sfg.beer.order.service.services.beerorder;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.sm.BeerOrderStateChangeInterceptor;
import guru.sfg.brewery.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

    public static final String ORDER_ID_HEADER = "ORDER_ID_HEADER";

    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);

        BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(beerOrder);
        sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);
        return savedBeerOrder;
    }

    @Transactional
    @Override
    public void processValidationResult(UUID beerOrderId, Boolean isValid) {
        log.debug("Process Validation Result for beerOrderId: " + beerOrderId + " is Valid? " + isValid);

        // NACHO he tenido que añadir esto por que al lanzar los tests fallaba ya que el process se ejecutaba antes de que se
        // hubiera realizado el save de la order en BD
        awaitForStatus(beerOrderId, BeerOrderStatusEnum.VALIDATION_PENDING);

        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderId);
        log.debug("beerOrderOptional: " + beerOrderOptional.isPresent());

        //        BeerOrder beerOrder = beerOrderRepository.getById(beerOrderId);
        //        log.debug("beerOrder: " + beerOrder.getId());

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            if (isValid) {
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);

                //wait for status change
                awaitForStatus(beerOrderId, BeerOrderStatusEnum.VALIDATED);

                log.debug("after awaitForStatus: ");

                // Tenemos que recuperar una nueva beerOrder por que la antenrior ha cambiado al enviar el nuevo evento
                // usando sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED)
                BeerOrder validatedOrder = beerOrderRepository.findById(beerOrderId).get();

                sendBeerOrderEvent(validatedOrder, BeerOrderEventEnum.ALLOCATE_ORDER);

            } else {
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
            }
        }, () -> log.error("processValidationResult - Order Not Found. Id: " + beerOrderId));
    }

    @Override
    public void beerOrderAllocationPassed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);
            awaitForStatus(beerOrder.getId(), BeerOrderStatusEnum.ALLOCATED);
            updateAllocatedQty(beerOrderDto);
        }, () -> log.error("beerOrderAllocationPassed - Order Id Not Found: " + beerOrderDto.getId()));
    }

    @Override
    public void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
            awaitForStatus(beerOrder.getId(), BeerOrderStatusEnum.PENDING_INVENTORY);
            updateAllocatedQty(beerOrderDto);
        }, () -> log.error("beerOrderAllocationPendingInventory - Order Id Not Found: " + beerOrderDto.getId()));

    }

    private void updateAllocatedQty(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> allocatedOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

        allocatedOrderOptional.ifPresentOrElse(allocatedOrder -> {
            allocatedOrder.getBeerOrderLines().forEach(beerOrderLine -> {
                beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
                    if (beerOrderLine.getId().equals(beerOrderLineDto.getId())) {
                        beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
                    }
                });
            });

            beerOrderRepository.saveAndFlush(allocatedOrder);
        }, () -> log.error("updateAllocatedQty - Order Not Found. Id: " + beerOrderDto.getId()));
    }

    @Override
    public void beerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
        }, () -> log.error("beerOrderAllocationFailed - Order Not Found. Id: " + beerOrderDto.getId()));

    }

    @Override
    public void beerOrderPickedUp(UUID id) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(id);

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            //do process
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.BEERORDER_PICKED_UP);
        }, () -> log.error("beerOrderPickedUp - Order Not Found. Id: " + id));
    }

    @Override
    public void cancelOrder(UUID id) {
        beerOrderRepository.findById(id).ifPresentOrElse(beerOrder -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.CANCEL_ORDER);
        }, () -> log.error("cancelOrder - Order Not Found. Id: " + id));
    }

    /**
     * Método que espera a que la maquina de estados actualice el estado de la orden al estado especificado
     * Es un workaround que hizo John para que pase los test de validación.
     *
     * @param beerOrderId
     * @param status
     */
    private void awaitForStatus(UUID beerOrderId, BeerOrderStatusEnum status) {

        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger loopCount = new AtomicInteger(0);

        while (!found.get()) {
            if (loopCount.incrementAndGet() > 20) {
                found.set(true);
                log.debug("awaitForStatus - Loop Retries exceeded");
            }

            beerOrderRepository.findById(beerOrderId).ifPresentOrElse(beerOrder -> {
                if (beerOrder.getOrderStatus().equals(status)) {
                    found.set(true);
                    log.debug("awaitForStatus - Order Found");
                } else {
                    log.debug("awaitForStatus - Order Status Not Equal. Expected: " + status.name() + " Found: " + beerOrder.getOrderStatus().name());
                }
            }, () -> log.debug("awaitForStatus - Order Id Not Found"));

            if (!found.get()) {
                try {
                    log.debug("awaitForStatus - Sleeping for retry");
                    Thread.sleep(100);
                } catch (Exception e) {
                    // do nothing
                    log.error("awaitForStatus - Sleeping Exception: " + e.getMessage());
                }
            }
        }
    }

    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum event) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = build(beerOrder);

        Message msg = MessageBuilder.withPayload(event)
                .setHeader(ORDER_ID_HEADER, beerOrder.getId().toString())
                .build();

        log.debug("sendBeerOrderEvent msg: " + msg);
        sm.sendEvent(msg);
    }

    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = stateMachineFactory.getStateMachine(beerOrder.getId());
        sm.stop();
        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null, null, null));
                });

        sm.start();
        return sm;
    }
}
