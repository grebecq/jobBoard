package fedoseev.jobboard.controller;

import fedoseev.jobboard.dto.request.CompanyRequest;
import fedoseev.jobboard.dto.response.CompanyResponse;
import fedoseev.jobboard.service.CompanyService;
import fedoseev.jobboard.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Компании", description = "Работодатели, публикующие вакансии")
@RequestMapping("/api/companies")
@RestController
public class CompanyController {
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @Operation(summary = "Создать компанию", description = "Текущий работодатель становится её владельцем.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CompanyResponse createCompany(@Valid @RequestBody CompanyRequest request, Authentication authentication){
       return companyService.createCompany(request, authentication.getName());
    }

    @Operation(summary = "Изменить компанию", description = "Только владелец (или ADMIN). Здесь же задаются контакты для связи: email и Telegram.")
    @PutMapping("/{id}")
    public CompanyResponse updateCompany(@PathVariable Long id,
                                         @Valid @RequestBody CompanyRequest request,
                                         Authentication authentication) {
        return companyService.updateCompany(id, request, authentication.getName(), SecurityUtils.isAdmin(authentication));
    }

    @Operation(summary = "Мои компании", description = "Компании текущего работодателя — от их имени он публикует вакансии.")
    @GetMapping("/my")
    public List<CompanyResponse> getMyCompanies(Authentication authentication) {
        return companyService.getMyCompanies(authentication.getName());
    }

    @Operation(summary = "Список компаний", description = "С пагинацией.")
    @GetMapping
    public Page<CompanyResponse> getAllCompanies(Pageable pageable) {
        return companyService.getAllCompanies(pageable);
    }

    @Operation(summary = "Компания по id")
    @GetMapping("/{id}")
    public  CompanyResponse getCompanyById(@PathVariable Long id){
        return companyService.getCompanyById(id);
    }
}
