package fedoseev.jobboard.service;

import fedoseev.jobboard.dto.request.VacancyRequest;
import fedoseev.jobboard.dto.response.VacancyResponse;
import fedoseev.jobboard.entity.Company;
import fedoseev.jobboard.entity.Skill;
import fedoseev.jobboard.entity.Vacancy;
import fedoseev.jobboard.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import fedoseev.jobboard.repository.ApplicationRepository;
import fedoseev.jobboard.repository.CompanyRepository;
import fedoseev.jobboard.repository.SkillRepository;
import fedoseev.jobboard.repository.VacancyApplicationStats;
import fedoseev.jobboard.repository.VacancyRepository;
import fedoseev.jobboard.specifications.Specifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class VacancyService {
    private final  VacancyRepository vacancyRepository;
    private final SkillRepository skillRepository;
    private final  CompanyRepository companyRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional
    public VacancyResponse createdVacancy(VacancyRequest request, String email, boolean isAdmin){
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() ->  new ResourceNotFoundException("Company not found "));

        if (!isAdmin && (company.getOwner() == null || !company.getOwner().getEmail().equals(email))) {
            throw new AccessDeniedException("Вакансию можно публиковать только от имени своей компании");
        }

        Vacancy vacancy = new Vacancy();
        vacancy.setSalaryTo(request.getSalaryTo());
        vacancy.setDescription(request.getDescription());
        vacancy.setTitle(request.getTitle());
        vacancy.setCity(request.getCity());
        vacancy.setEmploymentType(request.getEmploymentType());
        vacancy.setSalaryFrom(request.getSalaryFrom());
        vacancy.setCompany(company);

        if (request.getSkillIds() != null && !request.getSkillIds().isEmpty()) {
            List<Skill> skills = skillRepository.findAllById(request.getSkillIds());
            vacancy.setSkills(new HashSet<>(skills));
        }

        Vacancy saved = vacancyRepository.save(vacancy);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<VacancyResponse> getAllVacancies(Pageable pageable){
        return vacancyRepository.findAll(pageable)
                .map(this::mapToResponse);

    }

    @Transactional(readOnly = true)
    public Page<VacancyResponse> getMyVacancies(String email, Pageable pageable){
        Page<VacancyResponse> page = vacancyRepository.findByCompany_Owner_Email(email, pageable)
                .map(this::mapToResponse);
        if (page.isEmpty()) {
            return page;
        }

        // один group-by запрос на всю страницу вместо count() на каждую вакансию
        List<Long> ids = page.getContent().stream().map(VacancyResponse::getId).toList();
        Map<Long, VacancyApplicationStats> stats = applicationRepository.statsByVacancyIds(ids).stream()
                .collect(Collectors.toMap(VacancyApplicationStats::getVacancyId, Function.identity()));

        page.forEach(response -> {
            VacancyApplicationStats s = stats.get(response.getId());
            response.setApplicationsCount(s == null ? 0L : s.getTotal());
            response.setNewApplicationsCount(s == null ? 0L : s.getFresh());
        });
        return page;
    }

    @Transactional(readOnly = true)
    public VacancyResponse getVacancyById(Long id) {
        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));
        return mapToResponse(vacancy);
    }

    private VacancyResponse mapToResponse(Vacancy vacancy) {
        VacancyResponse response = new VacancyResponse();
        response.setId(vacancy.getId());
        response.setTitle(vacancy.getTitle());
        response.setDescription(vacancy.getDescription());
        response.setSalaryFrom(vacancy.getSalaryFrom());
        response.setSalaryTo(vacancy.getSalaryTo());
        response.setCity(vacancy.getCity());
        response.setEmploymentType(vacancy.getEmploymentType());
        response.setStatus(vacancy.getStatus());
        response.setCreatedAt(vacancy.getCreatedAt());
        response.setUpdatedAt(vacancy.getUpdatedAt());
        response.setCompanyId(vacancy.getCompany().getId());
        response.setCompanyName(vacancy.getCompany().getName());
        response.setSkillNames(
                vacancy.getSkills().stream()
                        .map(Skill::getName)
                        .collect(Collectors.toSet())
        );
        return response;
    }

    @Transactional(readOnly = true)
    public Page<VacancyResponse> searchVacancies(String city,
                                                 Integer minSalary,
                                                 String employmentType,
                                                 Pageable pageable
    ){
        Specification<Vacancy> spec = Specification.allOf(
                Specifications.hasCity(city),
                Specifications.salaryFromAtLeast(minSalary),
                Specifications.hasEmploymentType(employmentType)
        );

        return vacancyRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }
}
