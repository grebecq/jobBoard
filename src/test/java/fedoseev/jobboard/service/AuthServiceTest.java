package fedoseev.jobboard.service;

import fedoseev.jobboard.dto.request.LoginRequest;
import fedoseev.jobboard.dto.request.RegisterRequest;
import fedoseev.jobboard.dto.response.LoginResponse;
import fedoseev.jobboard.dto.response.RegisterResponse;
import fedoseev.jobboard.entity.Candidate;
import fedoseev.jobboard.entity.User;
import fedoseev.jobboard.enums.Role;
import fedoseev.jobboard.exception.BadRequestException;
import fedoseev.jobboard.exception.DuplicateResourceException;
import fedoseev.jobboard.repository.CandidateRepository;
import fedoseev.jobboard.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CandidateRepository candidateRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_whenEmailTaken_throwsDuplicate() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@mail.ru");
        request.setPassword("secret123");
        request.setRole(Role.CANDIDATE);

        when(userRepository.existsByEmail("test@mail.ru")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> authService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_whenValid_ReturnsResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@mail.ru");
        request.setPassword("secret123");
        request.setRole(Role.CANDIDATE);

        when(userRepository.existsByEmail("new@mail.ru")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("HASHED");
        when(candidateRepository.findByEmail("new@mail.ru")).thenReturn(Optional.empty());

        User saved = new User();
        saved.setId(1L);
        saved.setEmail("new@mail.ru");
        saved.setRole(Role.CANDIDATE);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        RegisterResponse response = authService.register(request);

        assertEquals(1L, response.getId());
        assertEquals("new@mail.ru", response.getEmail());
        assertEquals(Role.CANDIDATE, response.getRole());
        verify(passwordEncoder).encode("secret123");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("HASHED", captor.getValue().getPassword());
        assertTrue(captor.getValue().getEnabled());
    }

    @Test
    void register_whenCandidate_createsLinkedProfile() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("cand@mail.ru");
        request.setPassword("secret123");
        request.setRole(Role.CANDIDATE);

        when(userRepository.existsByEmail("cand@mail.ru")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("HASHED");
        when(candidateRepository.findByEmail("cand@mail.ru")).thenReturn(Optional.empty());

        User saved = new User();
        saved.setId(5L);
        saved.setEmail("cand@mail.ru");
        saved.setRole(Role.CANDIDATE);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        authService.register(request);

        ArgumentCaptor<Candidate> captor = ArgumentCaptor.forClass(Candidate.class);
        verify(candidateRepository).save(captor.capture());
        assertEquals("cand@mail.ru", captor.getValue().getEmail());
        assertEquals(5L, captor.getValue().getUser().getId());
    }

    @Test
    void register_whenEmployer_doesNotCreateCandidateProfile() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("empl@mail.ru");
        request.setPassword("secret123");
        request.setRole(Role.EMPLOYER);

        when(userRepository.existsByEmail("empl@mail.ru")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("HASHED");

        User saved = new User();
        saved.setId(6L);
        saved.setEmail("empl@mail.ru");
        saved.setRole(Role.EMPLOYER);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        authService.register(request);

        verify(candidateRepository, never()).save(any());
    }

    @Test
    void register_whenRoleAdmin_throwsBadRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("admin@mail.ru");
        request.setPassword("secret123");
        request.setRole(Role.ADMIN);

        when(userRepository.existsByEmail("admin@mail.ru")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_whenValid_returnsToken() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setPassword("HASHED");
        user.setRole(Role.CANDIDATE);

        when(userRepository.findByEmail("test@mail.ru")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "HASHED")).thenReturn(true);
        when(jwtService.generateToken("test@mail.ru")).thenReturn("JWT");

        LoginRequest request = new LoginRequest();
        request.setEmail("test@mail.ru");
        request.setPassword("secret123");

        LoginResponse response = authService.login(request);

        assertEquals("JWT", response.getToken());
    }

    @Test
    void login_whenWrongPassword_throwsBadCredentials() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setPassword("HASHED");

        when(userRepository.findByEmail("test@mail.ru")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "HASHED")).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@mail.ru");
        request.setPassword("wrong");

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_whenUnknownEmail_throwsBadCredentials() {
        when(userRepository.findByEmail("ghost@mail.ru")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        request.setEmail("ghost@mail.ru");
        request.setPassword("secret123");

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}
