package com.siemens.internship;

import com.siemens.internship.model.Item;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InternshipApplicationTests {

	@BeforeAll
	static void setUp() {
		RestAssured.baseURI = "http://localhost:8080/api/items";
	}

	@Test
	void shouldCreateItemWithValidData() {
		Item item = new Item();
		item.setId(1L);
		item.setEmail("rest@test.com");
		item.setDescription("Test desc");
		item.setStatus("ACTIVE");
		item.setName("RestValid");

		given()
				.contentType(ContentType.JSON)
				.body(item)
				.when()
				.post()
				.then()
				.statusCode(HttpStatus.CREATED.value())
				.body("email", equalTo("rest@test.com"))
				.body("name", equalTo("RestValid"));
	}

	@Test
	void shouldRejectInvalidEmails() {
		List<String> invalidEmails = List.of(
				"plainaddress", "@missing.com", "username@.com", "user@domain..com"
		);

		for (String email : invalidEmails) {
			Item item = new Item();
			item.setId(2L);
			item.setEmail(email);
			item.setDescription("Test desc");
			item.setStatus("ACTIVE");
			item.setName("BadEmail");

			given()
					.contentType(ContentType.JSON)
					.body(item)
					.when()
					.post()
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
		}
	}

	@Test
	void shouldRejectInvalidStatuses() {
		List<String> invalidStatuses = List.of("UNKNOWN", "INACTIVE_STATUS");

		for (String status : invalidStatuses) {
			Item item = new Item(1L,"BadStatus", "valid@mail.com", "desc", status);

			given()
					.contentType(ContentType.JSON)
					.body(item)
					.when()
					.post()
					.then()
					.statusCode(HttpStatus.BAD_REQUEST.value());
		}
	}

	@Test
	void shouldUpdateItem() {
		Item item = new Item(1L, "ToUpdate", "Description", "ACTIVE", "update@mail.com");

		int id = given()
				.contentType(ContentType.JSON)
				.body(item)
				.when()
				.post()
				.then()
				.statusCode(HttpStatus.CREATED.value())
				.extract().path("id");

		item.setName("Updated");
		item.setEmail("updated@mail.com");

		given()
				.contentType(ContentType.JSON)
				.body(item)
				.when()
				.put("/" + id)
				.then()
				.statusCode(HttpStatus.OK.value());

		given()
				.when()
				.get("/" + id)
				.then()
				.statusCode(HttpStatus.OK.value())
				.body("email", equalTo("updated@mail.com"))
				.body("name", equalTo("Updated"));
	}

	@Test
	void shouldDeleteItem() {
		Item item = new Item(3L, "ToDelete", "Item to be deleted", "ACTIVE", "delete@mail.com");

		int id = given()
				.contentType(ContentType.JSON)
				.body(item)
				.when()
				.post()
				.then()
				.statusCode(HttpStatus.CREATED.value())
				.extract().path("id");

		when()
				.delete("/" + id)
				.then()
				.statusCode(HttpStatus.NO_CONTENT.value());

		when()
				.get("/" + id)
				.then()
				.statusCode(HttpStatus.NOT_FOUND.value());
	}

	@Test
	void shouldProcessItemsAsyncSuccessfully() {
		List<Item> items = List.of(
				new Item(1L, "Proc1", "Item1", "ACTIVE", "proc1@mail.com"),
				new Item(2L, "Proc2", "Item2", "ACTIVE", "proc2@mail.com")
		);

		for (Item item : items) {
			given().contentType(ContentType.JSON).body(item).post();
		}

		when()
				.get("/process")
				.then()
				.statusCode(HttpStatus.OK.value())
				.body("size()", greaterThanOrEqualTo(2))
				.body("status", everyItem(equalTo("PROCESSED")));
	}
}
