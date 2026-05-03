package helen.com.authservice.messaging.producer;

import helen.com.authservice.config.RabbitMQConfig;
import helen.com.authservice.messaging.event.UserCreatedEvent;
import helen.com.authservice.messaging.event.UserLoggedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthProducer {
    private final RabbitTemplate rabbitTemplate;

    public void publishUserCreated(UserCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.USER_CREATED_ROUTING_KEY,
                event
        );
    }

    public void publishUserLogged(UserLoggedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.USER_LOGGED_ROUTING_KEY,
                event
        );
    }

}
