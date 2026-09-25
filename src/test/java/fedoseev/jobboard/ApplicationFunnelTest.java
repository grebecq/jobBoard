package fedoseev.jobboard;

import com.jayway.jsonpath.JsonPath;
import fedoseev.jobboard.dto.request.RegisterRequest;
import fedoseev.jobboard.enums.Role;
import fedoseev.jobboard.service.AuthService;
import fedoseev.jobboard.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApplicationFunnelTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtService jwtService;

    private String bearerFor(Role role) {
        String email = "funnel_" + UUID.randomUUID() + "@test.local";
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("secret123");
        request.setRole(role);
        authService.register(request);
        return "Bearer " + jwtService.generateToken(email);
    }

    private long idOf(String json) {
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    @Test
    void fullFunnel_contactsRevealedOnlyAfterInvite() throws Exception {
        String employer = bearerFor(Role.EMPLOYER);
        String candidate = bearerFor(Role.CANDIDATE);
        String stranger = bearerFor(Role.EMPLOYER);

        long companyId = idOf(mockMvc.perform(post("/api/companies")
                        .header("Authorization", employer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Funnel Co\",\"contactEmail\":\"hr@funnel.co\",\"telegram\":\"https://t.me/funnel_hr\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.telegram").value("funnel_hr"))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(get("/api/companies/" + companyId))
                .andExpect(jsonPath("$.telegram").value(nullValue()))
                .andExpect(jsonPath("$.contactEmail").value(nullValue()));

        long vacancyId = idOf(mockMvc.perform(post("/api/vacancies")
                        .header("Authorization", employer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Java\",\"description\":\"Spring\",\"companyId\":" + companyId + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        long applicationId = idOf(mockMvc.perform(post("/api/applications/apply/" + vacancyId)
                        .header("Authorization", candidate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"coverLetter\":\"Люблю Spring\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.coverLetter").value("Люблю Spring"))
                .andExpect(jsonPath("$.companyTelegram").value(nullValue()))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(post("/api/applications/apply/" + vacancyId).header("Authorization", candidate))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/vacancies/my").header("Authorization", employer))
                .andExpect(jsonPath("$.content[0].applicationsCount").value(1))
                .andExpect(jsonPath("$.content[0].newApplicationsCount").value(1));

        mockMvc.perform(get("/api/applications/vacancy/" + vacancyId).header("Authorization", stranger))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/applications/vacancy/" + vacancyId).header("Authorization", candidate))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/applications/vacancy/" + vacancyId).header("Authorization", employer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].coverLetter").value("Люблю Spring"));

        mockMvc.perform(get("/api/applications/" + applicationId).header("Authorization", employer))
                .andExpect(jsonPath("$.status").value("VIEWED"));

        mockMvc.perform(patch("/api/applications/" + applicationId + "/status")
                        .header("Authorization", stranger)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INVITED\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/applications/" + applicationId + "/status")
                        .header("Authorization", employer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INVITED\",\"comment\":\"Напишите в телеграм\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INVITED"));

        mockMvc.perform(get("/api/applications/my").header("Authorization", candidate))
                .andExpect(jsonPath("$[0].status").value("INVITED"))
                .andExpect(jsonPath("$[0].employerComment").value("Напишите в телеграм"))
                .andExpect(jsonPath("$[0].companyName").value("Funnel Co"))
                .andExpect(jsonPath("$[0].companyTelegram").value("funnel_hr"))
                .andExpect(jsonPath("$[0].companyContactEmail").value("hr@funnel.co"));
    }

    @Test
    void strangerCannotTouchForeignCompany() throws Exception {
        String owner = bearerFor(Role.EMPLOYER);
        String stranger = bearerFor(Role.EMPLOYER);

        long companyId = idOf(mockMvc.perform(post("/api/companies")
                        .header("Authorization", owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Mine\"}"))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(put("/api/companies/" + companyId)
                        .header("Authorization", stranger)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hijacked\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/vacancies")
                        .header("Authorization", stranger)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Fake\",\"description\":\"x\",\"companyId\":" + companyId + "}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/companies/" + companyId)
                        .header("Authorization", owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Mine\",\"telegram\":\"bad\"}"))
                .andExpect(status().isBadRequest());
    }
}
