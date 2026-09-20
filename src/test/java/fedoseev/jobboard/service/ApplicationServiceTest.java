package fedoseev.jobboard.service;

import fedoseev.jobboard.dto.response.ApplicationResponse;
import fedoseev.jobboard.entity.Application;
import fedoseev.jobboard.entity.Candidate;
import fedoseev.jobboard.entity.Company;
import fedoseev.jobboard.entity.Vacancy;
import fedoseev.jobboard.exception.DuplicateResourceException;
import fedoseev.jobboard.exception.ResourceNotFoundException;
import fedoseev.jobboard.repository.ApplicationRepository;
import fedoseev.jobboard.repository.CandidateRepository;
import fedoseev.jobboard.repository.VacancyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private CandidateRepository candidateRepository;
    @Mock
    private VacancyRepository vacancyRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private Candidate candidate(Long id, String email) {
        Candidate candidate = new Candidate();
        candidate.setId(id);
        candidate.setEmail(email);
        return candidate;
    }

    private Vacancy vacancy(Long id) {
        Company company = new Company();
        company.setId(1L);
        company.setName("Яндекс");

        Vacancy vacancy = new Vacancy();
        vacancy.setId(id);
        vacancy.setTitle("Java-разработчик");
        vacancy.setCity("Москва");
        vacancy.setCompany(company);
        return vacancy;
    }

    @Test
    void applyToVacancy_usesCandidateFromToken() {
        when(candidateRepository.findByUser_Email("me@mail.ru")).thenReturn(Optional.of(candidate(3L, "me@mail.ru")));
        when(applicationRepository.existsByCandidateIdAndVacancyId(3L, 7L)).thenReturn(false);
        when(vacancyRepository.findById(7L)).thenReturn(Optional.of(vacancy(7L)));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        ApplicationResponse response = applicationService.applyToVacancy("me@mail.ru", 7L);

        assertEquals(3L, response.getCandidateId());
        assertEquals(7L, response.getVacancyId());

        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(captor.capture());
        assertEquals(3L, captor.getValue().getCandidate().getId());
    }

    @Test
    void applyToVacancy_whenAlreadyApplied_throwsDuplicate() {
        when(candidateRepository.findByUser_Email("me@mail.ru")).thenReturn(Optional.of(candidate(3L, "me@mail.ru")));
        when(applicationRepository.existsByCandidateIdAndVacancyId(3L, 7L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> applicationService.applyToVacancy("me@mail.ru", 7L));

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void applyToVacancy_whenNoCandidateProfile_throwsNotFound() {
        when(candidateRepository.findByUser_Email("empl@mail.ru")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.applyToVacancy("empl@mail.ru", 7L));

        verify(vacancyRepository, never()).findById(anyLong());
    }

    @Test
    void applyToVacancy_whenVacancyMissing_throwsNotFound() {
        when(candidateRepository.findByUser_Email("me@mail.ru")).thenReturn(Optional.of(candidate(3L, "me@mail.ru")));
        when(applicationRepository.existsByCandidateIdAndVacancyId(3L, 99L)).thenReturn(false);
        when(vacancyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.applyToVacancy("me@mail.ru", 99L));
    }

    @Test
    void myApplications_whenNoCandidateProfile_returnsEmptyList() {
        when(candidateRepository.findByUser_Email("empl@mail.ru")).thenReturn(Optional.empty());

        assertTrue(applicationService.myApplications("empl@mail.ru").isEmpty());
    }

    @Test
    void myApplications_returnsOnlyOwnApplications() {
        Candidate me = candidate(3L, "me@mail.ru");
        Application application = new Application();
        application.setId(1L);
        application.setCandidate(me);
        application.setVacancy(vacancy(7L));

        when(candidateRepository.findByUser_Email("me@mail.ru")).thenReturn(Optional.of(me));
        when(applicationRepository.findByCandidateId(3L)).thenReturn(List.of(application));

        List<ApplicationResponse> responses = applicationService.myApplications("me@mail.ru");

        assertEquals(1, responses.size());
        assertEquals(3L, responses.getFirst().getCandidateId());
    }
}
