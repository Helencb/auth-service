package helen.com.authservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "auth.exchange";
    public static final String USER_CREATED_QUEUE = "user.created.queue";
    public static final String USER_LOGGED_QUEUE = "user.logged.queue";
    public static final String USER_CREATED_ROUTING_KEY = "user.created";
    public static final String USER_LOGGED_ROUTING_KEY = "user.logged";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue userCreatedQueue() {
        return new Queue(USER_CREATED_QUEUE);
    }

    @Bean
    public Queue userLoggedQueue(){
        return new Queue(USER_LOGGED_QUEUE);
    }

    @Bean
    public Binding  userCreateBinding(
            Queue userCreatedQueue,
            TopicExchange exchange
    ) {
        return BindingBuilder
                .bind(userCreatedQueue)
                .to(exchange)
                .with(USER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding userLoggedBinding(
            Queue userLoggedQueue,
            TopicExchange exchange
    ) {
        return BindingBuilder
                .bind(userLoggedQueue)
                .to(exchange)
                .with(USER_LOGGED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
