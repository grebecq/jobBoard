package fedoseev.jobboard.controller;

import fedoseev.jobboard.dto.request.CompanyRequest;
import fedoseev.jobboard.dto.response.CompanyResponse;
import fedoseev.jobboard.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Компании", description = "Работодатели, публикующие вакансии")
@RequestMapping("/api/companies")
@RestController
public class CompanyController {
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @Operation(summary = "Создать компанию")
    @PostMapping
    public CompanyResponse createCompany(@Valid @RequestBody CompanyRequest request){
       return companyService.createCompany(request);
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
