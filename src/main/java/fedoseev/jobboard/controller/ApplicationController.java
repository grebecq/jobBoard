package fedoseev.jobboard.controller;

import fedoseev.jobboard.dto.request.ApplicationRequest;
import fedoseev.jobboard.dto.response.ApplicationResponse;
import fedoseev.jobboard.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Отклики", description = "Отклики кандидатов на вакансии")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @Operation(summary = "Создать отклик за кандидата (ADMIN)",
            description = "Служебный метод: создаёт отклик по явному candidateId. Повторный отклик той же пары -> 409.")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApplicationResponse createdApplication(@RequestBody @Valid ApplicationRequest request) {
        return applicationService.createdApplication(request);
    }

    @Operation(summary = "Откликнуться на вакансию", description = "Текущий пользователь откликается на вакансию по её id.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/apply/{vacancyId}")
    public ApplicationResponse apply(@PathVariable Long vacancyId, Authentication authentication) {
        return applicationService.applyToVacancy(authentication.getName(), vacancyId);
    }

    @Operation(summary = "Мои отклики", description = "Отклики текущего пользователя.")
    @GetMapping("/my")
    public List<ApplicationResponse> myApplications(Authentication authentication) {
        return applicationService.myApplications(authentication.getName());
    }
}
