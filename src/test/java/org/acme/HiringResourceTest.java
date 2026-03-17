package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class HiringResourceTest {
    @Test
    void testNewHireEndpoint() {
        given()
          .when().post("/hiring/newHire", "")
          .then()
             .statusCode(200)
             .body(is("Hello from Quarkus REST"));
    }

}