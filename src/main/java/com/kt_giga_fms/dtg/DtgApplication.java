package com.kt_giga_fms.dtg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DtgApplication {

    public static void main(String[] args) {
        SpringApplication.run(DtgApplication.class, args);
    }

}
