package com.iwellness.reviews.integration;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.iwellness.reviews.client.ServicioApiClient;
import com.iwellness.reviews.client.UserApiClient;

/**
 * Test configuration that declares mock beans for integration tests.
 * The mocks are configured in @BeforeEach methods in the test classes.
 */
@TestConfiguration
public class IntegrationTestConfig {

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private UserApiClient userApiClient;

    @MockBean
    private ServicioApiClient servicioApiClient;
}
