package fedoseev.jobboard.service;

import fedoseev.jobboard.dto.response.ApplicationResponse;
import fedoseev.jobboard.entity.Application;
import fedoseev.jobboard.entity.Candidate;
import fedoseev.jobboard.entity.Company;
import fedoseev.jobboard.entity.User;
import fedoseev.jobboard.entity.Vacancy;
import fedoseev.jobboard.dto.request.ApplicationStatusRequest;
import fedoseev.jobboard.dto.response.EmployerApplicationResponse;
import fedoseev.jobboard.enums.ApplicationStatus;
import fedoseev.jobboard.enums.VacancyStatus;
import fedoseev.jobboard.exception.BadRequestException;
import org.springframework.security.access.AccessDeniedException;
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

        ApplicationResponse response = applicationService.applyToVacancy("me@mail.ru", 7L, null);

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
                () -> applicationService.applyToVacancy("me@mail.ru", 7L, null));

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void applyToVacancy_whenNoCandidateProfile_throwsNotFound() {
        when(candidateRepository.findByUser_Email("empl@mail.ru")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.applyToVacancy("empl@mail.ru", 7L, null));

        verify(vacancyRepository, never()).findById(anyLong());
    }

    @Test
    void applyToVacancy_whenVacancyMissing_throwsNotFound() {
        when(candidateRepository.findByUser_Email("me@mail.ru")).thenReturn(Optional.of(candidate(3L, "me@mail.ru")));
        when(applicationRepository.existsByCandidateIdAndVacancyId(3L, 99L)).thenReturn(false);
        when(vacancyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.applyToVacancy("me@mail.ru", 99L, null));
    }

    @Test
    void applyToVacancy_savesTrimmedCoverLetter() {
        when(candidateRepository.findByUser_Email("me@mail.ru")).thenReturn(Optional.of(candidate(3L, "me@mail.ru")));
        when(applicationRepository.existsByCandidateIdAndVacancyId(3L, 7L)).thenReturn(false);
        when(vacancyRepository.findById(7L)).thenReturn(Optional.of(vacancy(7L)));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        ApplicationResponse response = applicationService.applyToVacancy("me@mail.ru", 7L, "  Хочу к вам  ");

        assertEquals("Хочу к вам", response.getCoverLetter());
    }

    @Test
    void applyToVacancy_whenVacancyClosed_throwsBadRequest() {
        Vacancy closed = vacancy(7L);
        closed.setStatus(VacancyStatus.CLOSED);
        when(candidateRepository.findByUser_Email("me@mail.ru")).thenReturn(Optional.of(candidate(3L, "me@mail.ru")));
        when(applicationRepository.existsByCandidateIdAndVacancyId(3L, 7L)).thenReturn(false);
        when(vacancyRepository.findById(7L)).thenReturn(Optional.of(closed));

        assertThrows(BadRequestException.class,
                () -> applicationService.applyToVacancy("me@mail.ru", 7L, null));

        verify(applicationRepository, never()).save(any());
    }

    private Application applicationOnOwnedVacancy(ApplicationStatus status) {
        User owner = new User();
        owner.setEmail("boss@mail.ru");
        Vacancy vacancy = vacancy(7L);
        vacancy.getCompany().setOwner(owner);

        Application application = new Application();
        application.setId(1L);
        application.setStatus(status);
        application.setCandidate(candidate(3L, "me@mail.ru"));
        application.setVacancy(vacancy);
        return application;
    }

    @Test
    void getForEmployer_whenPending_marksViewed() {
        Application application = applicationOnOwnedVacancy(ApplicationStatus.PENDING);
        when(applicationRepository.findWithDetailsById(1L)).thenReturn(Optional.of(application));

        EmployerApplicationResponse response = applicationService.getForEmployer(1L, "boss@mail.ru", false);

        assertEquals(ApplicationStatus.VIEWED, response.getStatus());
        assertEquals("me@mail.ru", response.getCandidateEmail());
    }

    @Test
    void getForEmployer_whenNotOwner_throwsAccessDenied() {
        Application application = applicationOnOwnedVacancy(ApplicationStatus.PENDING);
        when(applicationRepository.findWithDetailsById(1L)).thenReturn(Optional.of(application));

        assertThrows(AccessDeniedException.class,
                () -> applicationService.getForEmployer(1L, "stranger@mail.ru", false));
        assertEquals(ApplicationStatus.PENDING, application.getStatus());
    }

    @Test
    void changeStatus_toPending_throwsBadRequest() {
        ApplicationStatusRequest request = new ApplicationStatusRequest();
        request.setStatus(ApplicationStatus.PENDING);

        assertThrows(BadRequestException.class,
                () -> applicationService.changeStatus(1L, request, "boss@mail.ru", false));
    }

    @Test
    void changeStatus_invite_savesBlankCommentAsNull() {
        Application application = applicationOnOwnedVacancy(ApplicationStatus.VIEWED);
        when(applicationRepository.findWithDetailsById(1L)).thenReturn(Optional.of(application));
        when(applicationRepository.saveAndFlush(application)).thenReturn(application);

        ApplicationStatusRequest request = new ApplicationStatusRequest();
        request.setStatus(ApplicationStatus.INVITED);
        request.setComment("   ");

        EmployerApplicationResponse response = applicationService.changeStatus(1L, request, "boss@mail.ru", false);

        assertEquals(ApplicationStatus.INVITED, response.getStatus());
        assertNull(application.getEmployerComment());
    }

    @Test
    void myApplications_exposesCompanyContactsOnlyWhenInvited() {
        Application invited = applicationOnOwnedVacancy(ApplicationStatus.INVITED);
        invited.getVacancy().getCompany().setTelegram("yandex_hr");
        Application pending = applicationOnOwnedVacancy(ApplicationStatus.PENDING);
        pending.setId(2L);
        pending.getVacancy().getCompany().setTelegram("yandex_hr");

        when(candidateRepository.findByUser_Email("me@mail.ru")).thenReturn(Optional.of(candidate(3L, "me@mail.ru")));
        when(applicationRepository.findByCandidateIdOrderByCreatedAtDesc(3L)).thenReturn(List.of(invited, pending));

        List<ApplicationResponse> responses = applicationService.myApplications("me@mail.ru");

        assertEquals("yandex_hr", responses.get(0).getCompanyTelegram());
        assertEquals("boss@mail.ru", responses.get(0).getCompanyContactEmail());
        assertNull(responses.get(1).getCompanyTelegram());
        assertNull(responses.get(1).getCompanyContactEmail());
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
        when(applicationRepository.findByCandidateIdOrderByCreatedAtDesc(3L)).thenReturn(List.of(application));

        List<ApplicationResponse> responses = applicationService.myApplications("me@mail.ru");

        assertEquals(1, responses.size());
        assertEquals(3L, responses.getFirst().getCandidateId());
    }
}
