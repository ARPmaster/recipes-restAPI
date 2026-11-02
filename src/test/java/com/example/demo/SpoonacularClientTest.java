package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;

import com.example.demo.exception.SpoonacularException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.example.demo.exception.SpoonacularException.Reason;

class SpoonacularClientTest {

	private static final String SEARCH_URL =
			"https://api.test/recipes/complexSearch?query=pasta&number=2&offset=4";

	private MockRestServiceServer server;

	private SpoonacularClient client;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		server = MockRestServiceServer.bindTo(builder).build();
		client = new SpoonacularClient(new SpoonacularProperties("test-key", "https://api.test"), builder);
	}

	@Test
	void sendsKeyInHeaderAndParsesResponse() {
		server.expect(requestTo(SEARCH_URL))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header("x-api-key", "test-key"))
				.andRespond(withSuccess("""
						{
						  "results": [
						    {"id": 1, "title": "Pasta", "image": "https://img/1.jpg", "imageType": "jpg"}
						  ],
						  "offset": 4,
						  "number": 2,
						  "totalResults": 42
						}
						""", MediaType.APPLICATION_JSON));

		SpoonacularSearchResponse response = client.searchRecipes("pasta", 2, 4);

		server.verify();
		assertThat(response.results()).containsExactly(
				new SpoonacularSearchResponse.Result(1L, "Pasta", "https://img/1.jpg"));
		assertThat(response.offset()).isEqualTo(4);
		assertThat(response.number()).isEqualTo(2);
		assertThat(response.totalResults()).isEqualTo(42);
	}

	@Test
	void encodesSpecialCharactersInQuery() {
		server.expect(requestTo(
				"https://api.test/recipes/complexSearch?query=mac%20%26%20cheese%2Bextra&number=2&offset=4"))
				.andRespond(withSuccess("{\"results\":[],\"offset\":4,\"number\":2,\"totalResults\":0}", MediaType.APPLICATION_JSON));

		client.searchRecipes("mac & cheese+extra", 2, 4);

		server.verify();
	}

	@ParameterizedTest
	@CsvSource({
			"401, INVALID_API_KEY",
			"402, QUOTA_EXCEEDED",
			"429, QUOTA_EXCEEDED",
			"400, UPSTREAM_ERROR",
			"500, UPSTREAM_ERROR"
	})
	void translatesErrorStatus(int status, Reason expected) {
		server.expect(requestTo(SEARCH_URL)).andRespond(withStatus(HttpStatus.valueOf(status)));

		assertFailsWith(expected);
	}

	@Test
	void translatesConnectionFailure() {
		server.expect(requestTo(SEARCH_URL)).andRespond(withException(new IOException("sin conexión")));

		assertFailsWith(Reason.UPSTREAM_ERROR);
	}

	@Test
	void translatesUnreadableBody() {
		server.expect(requestTo(SEARCH_URL)).andRespond(withSuccess("no es json", MediaType.APPLICATION_JSON));

		assertFailsWith(Reason.UPSTREAM_ERROR);
	}

	private void assertFailsWith(Reason expected) {
		assertThatThrownBy(() -> client.searchRecipes("pasta", 2, 4))
				.isInstanceOfSatisfying(SpoonacularException.class,
						ex -> assertThat(ex.getReason()).isEqualTo(expected));
	}
}
