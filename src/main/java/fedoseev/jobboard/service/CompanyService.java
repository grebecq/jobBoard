package fedoseev.jobboard.service;

import fedoseev.jobboard.dto.request.CompanyRequest;
import fedoseev.jobboard.dto.response.CompanyResponse;
import fedoseev.jobboard.entity.Company;
import fedoseev.jobboard.entity.User;
import fedoseev.jobboard.exception.ResourceNotFoundException;
import fedoseev.jobboard.repository.CompanyRepository;
import fedoseev.jobboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service

public class CompanyService {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Transactional
    public CompanyResponse createCompany(CompanyRequest request, String ownerEmail){
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + ownerEmail));

        Company company = new Company();
        company.setDescription(request.getDescription());
        company.setLogoUrl(request.getLogoUrl());
        company.setName(request.getName());
        company.setWebsite(request.getWebsite());
        company.setOwner(owner);
        Company saved = companyRepository.save(company);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponse> getAllCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> getMyCompanies(String ownerEmail) {
        return companyRepository.findByOwner_Email(ownerEmail).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
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
        response.setOwnerId(company.getOwner() == null ? null : company.getOwner().getId());
        return response;
    }
}
