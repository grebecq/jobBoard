package fedoseev.jobboard.service;

import fedoseev.jobboard.dto.request.ApplicationRequest;
import fedoseev.jobboard.dto.request.ApplicationStatusRequest;
import fedoseev.jobboard.dto.response.ApplicationResponse;
import fedoseev.jobboard.dto.response.EmployerApplicationResponse;
import fedoseev.jobboard.entity.Application;
import fedoseev.jobboard.entity.Candidate;
import fedoseev.jobboard.entity.Company;
import fedoseev.jobboard.entity.Vacancy;
import fedoseev.jobboard.enums.ApplicationStatus;
import fedoseev.jobboard.enums.VacancyStatus;
import fedoseev.jobboard.exception.BadRequestException;
import fedoseev.jobboard.exception.DuplicateResourceException;
import fedoseev.jobboard.exception.ResourceNotFoundException;
import fedoseev.jobboard.repository.ApplicationRepository;
import fedoseev.jobboard.repository.CandidateRepository;
import fedoseev.jobboard.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    private final VacancyRepository vacancyRepository;

    @Transactional
    public ApplicationResponse createdApplication(ApplicationRequest request) {
        if (applicationRepository.existsByCandidateIdAndVacancyId(request.getCandidateId(), request.getVacancyId())) {
            throw new DuplicateResourceException("Candidate already applied to this vacancy: " + request.getCandidateId());
        }
        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));

        Vacancy vacancy = vacancyRepository.findById(request.getVacancyId())
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        Application application = new Application();
        application.setCandidate(candidate);
        application.setCoverLetter(request.getCoverLetter());
        application.setVacancy(vacancy);
        return mapToResponse(applicationRepository.save(application));
    }

    @Transactional
    public ApplicationResponse applyToVacancy(String email, Long vacancyId, String coverLetter) {
        Candidate candidate = candidateRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Профиль кандидата не найден"));

        if (applicationRepository.existsByCandidateIdAndVacancyId(candidate.getId(), vacancyId)) {
            throw new DuplicateResourceException("Вы уже откликались на эту вакансию");
        }

        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        if (vacancy.getStatus() != VacancyStatus.ACTIVE) {
            throw new BadRequestException("Вакансия закрыта, отклики больше не принимаются");
        }

        Application application = new Application();
        application.setCandidate(candidate);
        application.setVacancy(vacancy);
        application.setCoverLetter(blankToNull(coverLetter));
        return mapToResponse(applicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> myApplications(String email) {
        return candidateRepository.findByUser_Email(email)
                .map(candidate -> applicationRepository.findByCandidateIdOrderByCreatedAtDesc(candidate.getId())
                        .stream().map(this::mapToResponse).toList())
                .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public List<EmployerApplicationResponse> vacancyApplications(Long vacancyId, String email, boolean isAdmin) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));
        checkOwner(vacancy.getCompany(), email, isAdmin);

        return applicationRepository.findByVacancyIdOrderByCreatedAtDesc(vacancyId).stream()
                .map(this::mapToEmployerResponse)
                .toList();
    }

    @Transactional
    public EmployerApplicationResponse getForEmployer(Long id, String email, boolean isAdmin) {
        Application application = findOwnApplication(id, email, isAdmin);
        if (application.getStatus() == ApplicationStatus.PENDING) {
            application.setStatus(ApplicationStatus.VIEWED);
        }
        return mapToEmployerResponse(application);
    }

    @Transactional
    public EmployerApplicationResponse changeStatus(Long id, ApplicationStatusRequest request, String email, boolean isAdmin) {
        if (request.getStatus() == ApplicationStatus.PENDING) {
            throw new BadRequestException("Нельзя вернуть отклик в статус «На рассмотрении»");
        }
        Application application = findOwnApplication(id, email, isAdmin);
        application.setStatus(request.getStatus());
        application.setEmployerComment(blankToNull(request.getComment()));
        return mapToEmployerResponse(applicationRepository.saveAndFlush(application));
    }

    private Application findOwnApplication(Long id, String email, boolean isAdmin) {
        Application application = applicationRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Отклик не найден"));
        checkOwner(application.getVacancy().getCompany(), email, isAdmin);
        return application;
    }

    private void checkOwner(Company company, String email, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        if (company.getOwner() == null || !company.getOwner().getEmail().equals(email)) {
            throw new AccessDeniedException("Отклики видит только владелец вакансии");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ApplicationResponse mapToResponse(Application application) {
        Vacancy vacancy = application.getVacancy();
        Company company = vacancy.getCompany();

        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setStatus(application.getStatus());
        response.setCandidateId(application.getCandidate().getId());
        response.setVacancyId(vacancy.getId());
        response.setVacancyTitle(vacancy.getTitle());
        response.setVacancyCity(vacancy.getCity());
        response.setCompanyName(company.getName());
        response.setCoverLetter(application.getCoverLetter());
        response.setEmployerComment(application.getEmployerComment());
        response.setCreatedAt(application.getCreatedAt());
        response.setUpdatedAt(application.getUpdatedAt());

        if (application.getStatus() == ApplicationStatus.INVITED) {
            String contactEmail = company.getContactEmail();
            if (contactEmail == null && company.getOwner() != null) {
                contactEmail = company.getOwner().getEmail();
            }
            response.setCompanyContactEmail(contactEmail);
            response.setCompanyTelegram(company.getTelegram());
        }
        return response;
    }

    private EmployerApplicationResponse mapToEmployerResponse(Application application) {
        Candidate candidate = application.getCandidate();

        EmployerApplicationResponse response = new EmployerApplicationResponse();
        response.setId(application.getId());
        response.setStatus(application.getStatus());
        response.setCoverLetter(application.getCoverLetter());
        response.setEmployerComment(application.getEmployerComment());
        response.setCreatedAt(application.getCreatedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        response.setVacancyId(application.getVacancy().getId());
        response.setVacancyTitle(application.getVacancy().getTitle());
        response.setCandidateId(candidate.getId());
        response.setCandidateFirstName(candidate.getFirstName());
        response.setCandidateLastName(candidate.getLastName());
        response.setCandidateEmail(candidate.getEmail());
        response.setCandidatePhone(candidate.getPhone());
        response.setCandidateTelegram(candidate.getTelegram());
        response.setCandidateCity(candidate.getCity());
        return response;
    }
}
