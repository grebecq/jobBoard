package fedoseev.jobboard.controller;

import fedoseev.jobboard.dto.request.ApplicationRequest;
import fedoseev.jobboard.dto.request.ApplicationStatusRequest;
import fedoseev.jobboard.dto.request.ApplyRequest;
import fedoseev.jobboard.dto.response.ApplicationResponse;
import fedoseev.jobboard.dto.response.EmployerApplicationResponse;
import fedoseev.jobboard.service.ApplicationService;
import fedoseev.jobboard.util.SecurityUtils;
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

    @Operation(summary = "Откликнуться на вакансию",
            description = "Текущий кандидат откликается на активную вакансию. Тело необязательно: {\"coverLetter\": \"...\"}.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/apply/{vacancyId}")
    public ApplicationResponse apply(@PathVariable Long vacancyId,
                                     @RequestBody(required = false) @Valid ApplyRequest request,
                                     Authentication authentication) {
        String coverLetter = request == null ? null : request.getCoverLetter();
        return applicationService.applyToVacancy(authentication.getName(), vacancyId, coverLetter);
    }

    @Operation(summary = "Мои отклики",
            description = "Отклики текущего кандидата, новые сверху. При статусе INVITED в ответе есть контакты работодателя.")
    @GetMapping("/my")
    public List<ApplicationResponse> myApplications(Authentication authentication) {
        return applicationService.myApplications(authentication.getName());
    }

    @Operation(summary = "Отклики на вакансию", description = "Только владелец вакансии (или ADMIN). С контактами кандидатов.")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @GetMapping("/vacancy/{vacancyId}")
    public List<EmployerApplicationResponse> vacancyApplications(@PathVariable Long vacancyId, Authentication authentication) {
        return applicationService.vacancyApplications(vacancyId, authentication.getName(), SecurityUtils.isAdmin(authentication));
    }

    @Operation(summary = "Открыть отклик", description = "Для работодателя. Новый отклик (PENDING) при этом становится VIEWED.")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @GetMapping("/{id}")
    public EmployerApplicationResponse getApplication(@PathVariable Long id, Authentication authentication) {
        return applicationService.getForEmployer(id, authentication.getName(), SecurityUtils.isAdmin(authentication));
    }

    @Operation(summary = "Сменить статус отклика",
            description = "VIEWED / INVITED / REJECTED, с необязательным комментарием для кандидата. При INVITED кандидат увидит контакты компании.")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @PatchMapping("/{id}/status")
    public EmployerApplicationResponse changeStatus(@PathVariable Long id,
                                                    @RequestBody @Valid ApplicationStatusRequest request,
                                                    Authentication authentication) {
        return applicationService.changeStatus(id, request, authentication.getName(), SecurityUtils.isAdmin(authentication));
    }
}
