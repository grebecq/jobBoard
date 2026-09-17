package fedoseev.jobboard.service;

import fedoseev.jobboard.dto.request.ApplicationRequest;
import fedoseev.jobboard.dto.response.ApplicationResponse;
import fedoseev.jobboard.dto.response.VacancyResponse;
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

@RequiredArgsConstructor
@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;

    private final CandidateRepository candidateRepository;

    private final VacancyRepository vacancyRepository;

    public ApplicationResponse createdApplication(ApplicationRequest request){
       if(applicationRepository.existsByCandidateIdAndVacancyId(request.getCandidateId(),request.getVacancyId())){
           throw new DuplicateResourceException("Candidate already applied to this vacancy" + request.getCandidateId());
       }
       Candidate candidate = candidateRepository.findById(request.getCandidateId())
               .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));

      Vacancy vacancy =  vacancyRepository.findById(request.getVacancyId())
               .orElseThrow(()-> new ResourceNotFoundException("Vacancy not found"));

       Application application = new Application();
       application.setCandidate(candidate);
       application.setCoverLetter(request.getCoverLetter());
       application.setVacancy(vacancy);
       Application saved = applicationRepository.save(application);
       return mapToResponse(saved);
    }
    private ApplicationResponse mapToResponse(Application application){
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setStatus(application.getStatus());
        response.setCandidateId(application.getCandidate().getId());
        response.setVacancyId(application.getVacancy().getId());
        response.setCoverLetter(application.getCoverLetter());
        response.setCreatedAt(application.getCreatedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        return response;
    }

}
