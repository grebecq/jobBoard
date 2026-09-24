package fedoseev.jobboard.service;

import fedoseev.jobboard.dto.request.CompanyRequest;
import fedoseev.jobboard.dto.response.CompanyResponse;
import fedoseev.jobboard.entity.Company;
import fedoseev.jobboard.entity.User;
import fedoseev.jobboard.exception.ResourceNotFoundException;
import fedoseev.jobboard.repository.CompanyRepository;
import fedoseev.jobboard.repository.UserRepository;
import fedoseev.jobboard.util.TelegramHandle;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
        applyRequest(company, request);
        company.setOwner(owner);
        Company saved = companyRepository.save(company);

        return mapToResponse(saved, true);
    }

    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyRequest request, String email, boolean isAdmin) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        if (!isAdmin && (company.getOwner() == null || !company.getOwner().getEmail().equals(email))) {
            throw new AccessDeniedException("Редактировать компанию может только её владелец");
        }

        applyRequest(company, request);
        return mapToResponse(company, true);
    }

    private void applyRequest(Company company, CompanyRequest request) {
        company.setName(request.getName());
        company.setDescription(request.getDescription());
        company.setLogoUrl(request.getLogoUrl());
        company.setWebsite(request.getWebsite());
        company.setContactEmail(request.getContactEmail() == null || request.getContactEmail().isBlank()
                ? null : request.getContactEmail().trim());
        company.setTelegram(TelegramHandle.normalize(request.getTelegram()));
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponse> getAllCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable)
                .map(company -> mapToResponse(company, false));
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> getMyCompanies(String ownerEmail) {
        return companyRepository.findByOwner_Email(ownerEmail).stream()
                .map(company -> mapToResponse(company, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public  CompanyResponse getCompanyById(Long id){
       Company company = companyRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
       return mapToResponse(company, false);
    }

    // контакты компании публично не светим: кандидат получает их вместе с приглашением
    private CompanyResponse mapToResponse(Company company, boolean withContacts){
        CompanyResponse response = new CompanyResponse();
        response.setWebsite(company.getWebsite());
        response.setName(company.getName());
        response.setId(company.getId());
        response.setLogoUrl(company.getLogoUrl());
        response.setDescription(company.getDescription());
        response.setCreatedAt(company.getCreatedAt());
        response.setOwnerId(company.getOwner() == null ? null : company.getOwner().getId());
        if (withContacts) {
            response.setContactEmail(company.getContactEmail());
            response.setTelegram(company.getTelegram());
        }
        return response;
    }
}
