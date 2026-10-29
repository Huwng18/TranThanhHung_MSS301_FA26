package fu.de190481.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DE190481ReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DE190481ReservationServiceApplication.class, args);
    }
}
