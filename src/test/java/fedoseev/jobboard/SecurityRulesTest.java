package fedoseev.jobboard;

import fedoseev.jobboard.dto.request.RegisterRequest;
import fedoseev.jobboard.enums.Role;
import fedoseev.jobboard.service.AuthService;
import fedoseev.jobboard.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityRulesTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtService jwtService;

    private String tokenFor(Role role) {
        String email = "test_" + UUID.randomUUID() + "@test.local";
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("secret123");
        request.setRole(role);
        authService.register(request);
        return jwtService.generateToken(email);
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void spaIndex_isPublic() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void vacancyList_isPublic() throws Exception {
        mockMvc.perform(get("/api/vacancies"))
                .andExpect(status().isOk());
    }

    @Test
    void companyList_isPublic() throws Exception {
        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk());
    }

    @Test
    void me_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withBrokenToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer garbage.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withValidToken_returns200() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(tokenFor(Role.CANDIDATE))))
                .andExpect(status().isOk());
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@test.local\",\"password\":\"whatever1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createSkill_asCandidate_returns403() throws Exception {
        mockMvc.perform(post("/api/skills")
                        .header("Authorization", bearer(tokenFor(Role.CANDIDATE)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Kotlin\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCompany_asCandidate_returns403() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .header("Authorization", bearer(tokenFor(Role.CANDIDATE)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Fake Co\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createVacancy_asCandidate_returns403() throws Exception {
        mockMvc.perform(post("/api/vacancies")
                        .header("Authorization", bearer(tokenFor(Role.CANDIDATE)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Java\",\"description\":\"x\",\"companyId\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createApplicationByCandidateId_asCandidate_returns403() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .header("Authorization", bearer(tokenFor(Role.CANDIDATE)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"candidateId\":1,\"vacancyId\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCandidateProfile_asCandidate_returns403() throws Exception {
        mockMvc.perform(post("/api/candidates")
                        .header("Authorization", bearer(tokenFor(Role.CANDIDATE)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Ghost\",\"lastName\":\"Ghost\",\"email\":\"ghost@test.local\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownEnumValue_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@test.local\",\"password\":\"secret123\",\"role\":\"HACKER\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void badPathVariableType_returns400() throws Exception {
        mockMvc.perform(get("/api/vacancies/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownSortProperty_returns400() throws Exception {
        mockMvc.perform(get("/api/vacancies").param("sort", "nonExistentField,desc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingVacancy_returns404() throws Exception {
        mockMvc.perform(get("/api/vacancies/99999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void hugePageSize_isCapped() throws Exception {
        mockMvc.perform(get("/api/vacancies").param("size", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(100));
    }
}
