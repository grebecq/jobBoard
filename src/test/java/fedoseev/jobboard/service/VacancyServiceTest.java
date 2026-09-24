package fedoseev.jobboard.service;

import fedoseev.jobboard.dto.request.VacancyRequest;
import fedoseev.jobboard.dto.response.VacancyResponse;
import fedoseev.jobboard.entity.Company;
import fedoseev.jobboard.entity.User;
import fedoseev.jobboard.entity.Vacancy;
import fedoseev.jobboard.exception.ResourceNotFoundException;
import fedoseev.jobboard.repository.ApplicationRepository;
import fedoseev.jobboard.repository.CompanyRepository;
import fedoseev.jobboard.repository.SkillRepository;
import fedoseev.jobboard.repository.VacancyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacancyServiceTest {

    @Mock
    private VacancyRepository vacancyRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private VacancyService vacancyService;

    private Company companyOwnedBy(String ownerEmail) {
        Company company = new Company();
        company.setId(1L);
        company.setName("Яндекс");
        if (ownerEmail != null) {
            User owner = new User();
            owner.setId(10L);
            owner.setEmail(ownerEmail);
            company.setOwner(owner);
        }
        return company;
    }

    private VacancyRequest request() {
        VacancyRequest request = new VacancyRequest();
        request.setTitle("Java-разработчик");
        request.setDescription("Spring Boot");
        request.setCity("Москва");
        request.setCompanyId(1L);
        return request;
    }

    @Test
    void createdVacancy_whenOwnCompany_saves() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(companyOwnedBy("boss@mail.ru")));
        when(vacancyRepository.save(any(Vacancy.class))).thenAnswer(inv -> inv.getArgument(0));

        VacancyResponse response = vacancyService.createdVacancy(request(), "boss@mail.ru", false);

        assertEquals("Java-разработчик", response.getTitle());
        assertEquals(1L, response.getCompanyId());
        verify(vacancyRepository).save(any(Vacancy.class));
    }

    @Test
    void createdVacancy_whenForeignCompany_throwsAccessDenied() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(companyOwnedBy("boss@mail.ru")));

        assertThrows(AccessDeniedException.class,
                () -> vacancyService.createdVacancy(request(), "stranger@mail.ru", false));

        verify(vacancyRepository, never()).save(any());
    }

    @Test
    void createdVacancy_whenCompanyHasNoOwner_throwsAccessDenied() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(companyOwnedBy(null)));

        assertThrows(AccessDeniedException.class,
                () -> vacancyService.createdVacancy(request(), "boss@mail.ru", false));

        verify(vacancyRepository, never()).save(any());
    }

    @Test
    void createdVacancy_whenAdmin_allowsForeignCompany() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(companyOwnedBy("boss@mail.ru")));
        when(vacancyRepository.save(any(Vacancy.class))).thenAnswer(inv -> inv.getArgument(0));

        VacancyResponse response = vacancyService.createdVacancy(request(), "admin@mail.ru", true);

        assertEquals("Java-разработчик", response.getTitle());
    }

    @Test
    void createdVacancy_whenCompanyMissing_throwsNotFound() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> vacancyService.createdVacancy(request(), "boss@mail.ru", false));
    }
}
