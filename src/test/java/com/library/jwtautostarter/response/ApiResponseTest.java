package com.library.jwtautostarter.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void successFactoryMethodSetsSuccessTrueAndData() {
        ApiResponse<String> response = ApiResponse.success("hello");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("hello");
        assertThat(response.getMessage()).isEqualTo("OK");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void successWithMessageSetsCustomMessage() {
        ApiResponse<String> response = ApiResponse.success("hello", "Created");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Created");
        assertThat(response.getData()).isEqualTo("hello");
    }

    @Test
    void errorFactoryMethodSetsSuccessFalseAndNullData() {
        ApiResponse<Void> response = ApiResponse.error("Not found");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Not found");
        assertThat(response.getData()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void successResponseWithNullDataIsAllowed() {
        ApiResponse<Void> response = ApiResponse.success(null);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNull();
    }

    @Test
    void timestampIsSetOnCreation() {
        ApiResponse<String> response = ApiResponse.success("test");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void successResponseWithObjectData() {
        record Payload(String name, int age) {}
        Payload payload = new Payload("Alice", 30);

        ApiResponse<Payload> response = ApiResponse.success(payload);

        assertThat(response.getData()).isEqualTo(payload);
        assertThat(response.getData().name()).isEqualTo("Alice");
    }
}
