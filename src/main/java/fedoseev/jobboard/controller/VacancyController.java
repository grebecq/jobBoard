package fedoseev.jobboard.controller;

import fedoseev.jobboard.dto.request.VacancyRequest;
import fedoseev.jobboard.dto.response.VacancyResponse;
import fedoseev.jobboard.service.VacancyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Tag(name = "Вакансии", description = "Вакансии компаний, поиск и фильтрация")
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
@RestController
public class VacancyController {

    private final VacancyService vacancyService;

    @Operation(summary = "Создать вакансию", description = "Требует существующий companyId; можно передать skillIds.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public VacancyResponse createVacancy(@Valid @RequestBody VacancyRequest vacancyRequest){
        return vacancyService.createdVacancy(vacancyRequest);

    }

    @Operation(summary = "Список вакансий", description = "С пагинацией.")
    @GetMapping
    public Page<VacancyResponse> getAllVacancy(Pageable pageable){
        return vacancyService.getAllVacancies(pageable);
    }

    @Operation(summary = "Вакансия по id")
    @GetMapping("/{id}")
    public VacancyResponse getVacancyById(@PathVariable Long id){
        return vacancyService.getVacancyById(id);
    }

    @Operation(summary = "Поиск вакансий", description = "Динамические фильтры: город, минимальная зарплата, тип занятости. Любой можно не задавать.")
    @GetMapping("/search")
    public Page<VacancyResponse> searchVacancies(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer minSalary,
            @RequestParam(required = false) String employmentType,
            Pageable pageable ){
        return vacancyService.searchVacancies(city, minSalary, employmentType, pageable);
    }

}
