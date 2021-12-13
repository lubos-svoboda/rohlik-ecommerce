package cz.lubsvo.rohlik.ecomm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rohlik ECommerce API Case study")
                        .version("1.0")
                        .description("Simple REST interface to maintain a database of products and orders."));
    }
}
