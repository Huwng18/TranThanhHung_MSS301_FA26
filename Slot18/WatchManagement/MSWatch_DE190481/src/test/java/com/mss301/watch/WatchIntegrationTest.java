package com.mss301.watch;

import org.springframework.boot.test.context.SpringBootTest;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// [TODO-6.5] H2 verifies local transactions without requiring the SQL Server daemon.
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:watch-tests;MODE=MSSQLServer;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "saga.compensation.backoff-ms=0", "logging.level.root=WARN"
})
public @interface WatchIntegrationTest {
}
