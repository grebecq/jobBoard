package fedoseev.jobboard.service;

import fedoseev.jobboard.dto.request.CompanyRequest;
import fedoseev.jobboard.dto.response.CompanyResponse;
import fedoseev.jobboard.entity.Company;
import fedoseev.jobboard.exception.ResourceNotFoundException;
import fedoseev.jobboard.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@RequiredArgsConstructor
@Service

public class CompanyService {
    private final CompanyRepository companyRepository;

    
    public CompanyResponse createCompany(CompanyRequest request){
        Company company = new Company();
        company.setDescription(request.getDescription());
        company.setLogoUrl(request.getLogoUrl());
        company.setName(request.getName());
        company.setWebsite(request.getWebsite());
        Company saved = companyRepository.save(company);

        return mapToResponse(saved);
    }

    public Page<CompanyResponse> getAllCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public  CompanyResponse getCompanyById(Long id){
       Company company = companyRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
       return mapToResponse(company);
    }

    private CompanyResponse mapToResponse(Company company){
        CompanyResponse response = new CompanyResponse();
        response.setWebsite(company.getWebsite());
        response.setName(company.getName());
        response.setId(company.getId());
        response.setLogoUrl(company.getLogoUrl());
        response.setDescription(company.getDescription());
        response.setCreatedAt(company.getCreatedAt());
        return response;
    }
}
