package com.example.starter_kit_restapi_springboot;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

class StarterKitRestapiSpringbootApplicationTests {

    @Test
    void mainShouldDelegateToSpringApplicationRun() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            StarterKitRestapiSpringbootApplication.main(new String[]{"--test"});

            springApplication.verify(() -> SpringApplication.run(eq(StarterKitRestapiSpringbootApplication.class), any(String[].class)));
        }
    }
}
