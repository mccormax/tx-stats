package com.logicpole.txstats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring application entry-point
 *
 * This will cause the transaction stats rest service to come up at
 * localhost:8080.
 *
 * TODO:  make the application context configurable
 *
 * @author Max McCormick
 */
@SpringBootApplication
public class Application {

   public static void main(String[] args) {
      SpringApplication.run(Application.class, args);
   }
}
