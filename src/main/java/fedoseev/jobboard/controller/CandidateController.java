package fedoseev.jobboard.controller;

import fedoseev.jobboard.dto.request.CandidateRequest;
import fedoseev.jobboard.dto.response.CandidateResponse;
import fedoseev.jobboard.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Кандидаты", description = "Управление кандидатами")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    @Operation(summary = "Создать кандидата (ADMIN)", description = "Email должен быть уникальным, иначе 409.")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CandidateResponse createdCandidate(@RequestBody @Valid CandidateRequest request){
        return candidateService.createdCandidate(request);
    }
}
