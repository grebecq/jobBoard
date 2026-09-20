package fedoseev.jobboard.service;

import fedoseev.jobboard.dto.request.ApplicationRequest;
import fedoseev.jobboard.dto.response.ApplicationResponse;
import fedoseev.jobboard.entity.Application;
import fedoseev.jobboard.entity.Candidate;
import fedoseev.jobboard.entity.Vacancy;
import fedoseev.jobboard.exception.DuplicateResourceException;
import fedoseev.jobboard.exception.ResourceNotFoundException;
import fedoseev.jobboard.repository.ApplicationRepository;
import fedoseev.jobboard.repository.CandidateRepository;
import fedoseev.jobboard.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
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
            throw new DuplicateResourceException("Candidate already applied to this vacancy" + request.getCandidateId());
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
    public ApplicationResponse applyToVacancy(String email, Long vacancyId) {
        Candidate candidate = candidateRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Профиль кандидата не найден"));

        if (applicationRepository.existsByCandidateIdAndVacancyId(candidate.getId(), vacancyId)) {
            throw new DuplicateResourceException("Вы уже откликались на эту вакансию");
        }

        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        Application application = new Application();
        application.setCandidate(candidate);
        application.setVacancy(vacancy);
        return mapToResponse(applicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> myApplications(String email) {
        return candidateRepository.findByUser_Email(email)
                .map(candidate -> applicationRepository.findByCandidateId(candidate.getId())
                        .stream().map(this::mapToResponse).toList())
                .orElseGet(List::of);
    }

    private ApplicationResponse mapToResponse(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setStatus(application.getStatus());
        response.setCandidateId(application.getCandidate().getId());
        response.setVacancyId(application.getVacancy().getId());
        response.setVacancyTitle(application.getVacancy().getTitle());
        response.setVacancyCity(application.getVacancy().getCity());
        response.setCoverLetter(application.getCoverLetter());
        response.setCreatedAt(application.getCreatedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        return response;
    }
}
