package nl.tekle.mini.payments.api;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

// Starts the full application against the in-memory H2 database from application.properties.
@SpringBootTest
class TransferFlowIntegrationTest {

    private final MockMvc mockMvc;

    TransferFlowIntegrationTest(@Autowired WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void transferMovesMoneyBetweenAccounts() throws Exception {
        String aliceId = createAccount("Alice", "100.00");
        String bobId = createAccount("Bob", "50.00");

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromAccountId": "%s", "toAccountId": "%s", "amount": 30.00}
                                """.formatted(aliceId, bobId)))
                .andExpect(status().isCreated());

        // JsonPath reads JSON numbers as Double; that is fine for asserting on the response.
        mockMvc.perform(get("/api/accounts/{id}", aliceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(70.0));
        mockMvc.perform(get("/api/accounts/{id}", bobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(80.0));

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].owner", hasItems("Alice", "Bob")));
    }

    private String createAccount(String owner, String initialBalance) throws Exception {
        String response = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"owner": "%s", "initialBalance": %s}
                                """.formatted(owner, initialBalance)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id");
    }
}
