package fedoseev.jobboard.controller;

import fedoseev.jobboard.dto.request.ApplicationRequest;
import fedoseev.jobboard.dto.response.ApplicationResponse;
import fedoseev.jobboard.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Отклики", description = "Отклики кандидатов на вакансии")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private final ApplicationService applicationService;

    @Operation(summary = "Создать отклик", description = "Кандидат откликается на вакансию. Повторный отклик той же пары -> 409.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApplicationResponse createdApplication(@RequestBody @Valid ApplicationRequest request){
        return applicationService.createdApplication(request);
    }
}
