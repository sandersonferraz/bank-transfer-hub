package com.transaction.transferservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TransferServiceApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("Should load the TransferApplication context")
    void contextLoads() {
        assertThat(context).isNotNull();
        assertThat(context).isInstanceOf(ApplicationContext.class).isNotNull();

    }

}
