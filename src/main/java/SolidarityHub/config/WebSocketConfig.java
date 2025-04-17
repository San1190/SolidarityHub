package SolidarityHub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue"); // destinos a los que el cliente puede suscribirse
        config.setApplicationDestinationPrefixes("/app"); // prefijo para mensajes enviados del cliente al servidor
        config.setUserDestinationPrefix("/user"); // para mensajes privados tipo convertAndSendToUser
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // el endpoint que usará el cliente
                .setAllowedOriginPatterns("*")
                .withSockJS(); // habilita fallback con SockJS
    }
}
