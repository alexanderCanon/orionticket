package com.orionticket.seating;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada del microservicio seating-inventory.
 *
 * @EnableScheduling activa el scheduler de Spring, necesario para que
 * el ReservationExpirationJob pueda correr cada 60 segundos con @Scheduled.
 * Sin esta anotación, los métodos @Scheduled simplemente no se ejecutan.
 */
@SpringBootApplication
@EnableScheduling
public class SeatingInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeatingInventoryApplication.class, args);
    }
}
