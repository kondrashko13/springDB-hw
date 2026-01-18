package com.springdbhw;

import org.springframework.boot.SpringApplication;

public class TestSpringDbHwApplication {

    public static void main(String[] args) {
        SpringApplication.from(SpringDbHwApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
