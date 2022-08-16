package com.revealprecision.revealstreams.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(title = "Reveal Server"
        , version = "${springdoc.version}"
        , description = "Reveal Server forms the backend processing of the Reveal Platform"
        , license = @License(name = "Reveal Precision", url = "https://www.revealprecision.com")
        , contact = @Contact(name = "Akros Inc. ", email = "info@akros.com", url = "https://www.akros.com"
    ))
)
public class SwaggerConfig {

}
