package fedoseev.jobboard;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthAndAccessTest {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions register(String email, String password, String role) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"role\":\"" + role + "\"}"));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    private String bearer(String role) throws Exception {
        String email = "access_" + UUID.randomUUID() + "@test.local";
        register(email, "secret123", role).andExpect(status().isCreated());
        String json = login(email, "secret123").andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(json, "$.token");
    }

    @Test
    void registerAndLogin_ignoreEmailCaseAndSpaces() throws Exception {
        String id = UUID.randomUUID().toString().substring(0, 8);

        register(" Sasha." + id + "@Mail.RU ", "secret123", "CANDIDATE")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("sasha." + id + "@mail.ru"));

        login("SASHA." + id + "@mail.ru", "secret123").andExpect(status().isOk());

        register("sasha." + id + "@mail.ru", "secret123", "EMPLOYER")
                .andExpect(status().isConflict());
    }

    @Test
    void wrongPassword_returns401() throws Exception {
        String email = "wrong_" + UUID.randomUUID() + "@test.local";
        register(email, "secret123", "CANDIDATE");

        login(email, "secret999")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Неверный email или пароль"));
    }

    @Test
    void invalidInput_returnsReadableError() throws Exception {
        register("short_" + UUID.randomUUID() + "@test.local", "123", "CANDIDATE")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Пароль должен быть от 6 до 100 символов"));

        register("not-an-email", "secret123", "CANDIDATE")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email указан неверно"));

        register("admin_" + UUID.randomUUID() + "@test.local", "secret123", "ADMIN")
                .andExpect(status().isBadRequest());
    }

    @Test
    void accessRules() throws Exception {
        mockMvc.perform(get("/api/vacancies")).andExpect(status().isOk());
        mockMvc.perform(get("/api/dictionaries")).andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer garbage.token.here"))
                .andExpect(status().isUnauthorized());

        String candidate = bearer("CANDIDATE");
        mockMvc.perform(get("/api/auth/me").header("Authorization", candidate)).andExpect(status().isOk());
        mockMvc.perform(post("/api/companies").header("Authorization", candidate)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Fake\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/vacancies").header("Authorization", candidate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Java\",\"description\":\"x\",\"companyId\":1}"))
                .andExpect(status().isForbidden());
    }
}
