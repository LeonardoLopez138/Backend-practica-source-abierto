package com.acme.nubi.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ComAcmeNubiPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComAcmeNubiPlatformApplication.class, args);
    }

}
