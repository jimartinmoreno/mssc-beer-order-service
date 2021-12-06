package guru.sfg.beer.order.service.services.testcomponets;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.brewery.model.events.ValidateOrderRequest;
import guru.sfg.brewery.model.events.ValidateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Se define para procesar los mensajes de validación simulando lo que haria el beer service
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidationListener {
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
//    public void listen(Message msg) {
    public void listen(ValidateOrderRequest validateOrderRequest) {
        boolean isValid = true;
        boolean sendResponse = true;

        log.debug("############# listen - validateOrderRequest: " + validateOrderRequest);
        // ValidateOrderRequest validateOrderRequest = (ValidateOrderRequest) msg.getPayload();

        //condition to fail validation
        if (validateOrderRequest.getBeerOrder().getCustomerRef() != null) {
            if (validateOrderRequest.getBeerOrder().getCustomerRef().equals("fail-validation")) {
                isValid = false;
            } else if (validateOrderRequest.getBeerOrder().getCustomerRef().equals("dont-validate")) {
                sendResponse = false;
            }
        }

        if (sendResponse) {
            jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE,
                    ValidateOrderResult.builder()
                            .isValid(isValid)
                            .orderId(validateOrderRequest.getBeerOrder().getId())
                            .build());
        }
    }
}
