package com.northernchile.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories("com.northernchile.api")
public class ApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(ApiApplication.class, args);
  }

}
