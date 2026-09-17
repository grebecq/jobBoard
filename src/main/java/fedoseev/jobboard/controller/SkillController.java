package fedoseev.jobboard.controller;

import fedoseev.jobboard.dto.request.SkillRequest;
import fedoseev.jobboard.dto.response.SkillResponse;
import fedoseev.jobboard.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Навыки", description = "Справочник навыков (тегов)")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать навык", description = "Имя навыка уникально, иначе 409.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public SkillResponse createSkill(@Valid @RequestBody SkillRequest request){
        return skillService.createSkill(request);
    }
}
