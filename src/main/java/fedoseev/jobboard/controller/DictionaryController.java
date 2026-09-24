package fedoseev.jobboard.controller;

import fedoseev.jobboard.dto.response.DictionariesResponse;
import fedoseev.jobboard.dto.response.DictionariesResponse.Option;
import fedoseev.jobboard.enums.EmploymentType;
import fedoseev.jobboard.enums.Experience;
import fedoseev.jobboard.enums.Grade;
import fedoseev.jobboard.enums.SkillCategory;
import fedoseev.jobboard.enums.Specialization;
import fedoseev.jobboard.enums.WorkFormat;
import fedoseev.jobboard.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Справочники", description = "Значения фильтров и справочник технологий")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/dictionaries")
public class DictionaryController {

    private final SkillService skillService;

    @Operation(summary = "Все справочники",
            description = "Специализации, грейды, опыт, формат работы, тип занятости (value + подпись) и все навыки с категориями.")
    @GetMapping
    public DictionariesResponse getAll() {
        return new DictionariesResponse(
                Option.of(Specialization.class),
                Option.of(Grade.class),
                Option.of(Experience.class),
                Option.of(WorkFormat.class),
                Option.of(EmploymentType.class),
                Option.of(SkillCategory.class),
                skillService.getAllSkills()
        );
    }
}
