package com.revealprecision.revealstreams.props;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "reveal.instance")
@Setter
@Getter
public class InstanceProperties {

    String client = "uw";
}
