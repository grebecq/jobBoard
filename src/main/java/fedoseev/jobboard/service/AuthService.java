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
import fedoseev.jobboard.exception.ResourceNotFoundException;
import fedoseev.jobboard.repository.CandidateRepository;
import fedoseev.jobboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = normalize(request.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Этот email уже зарегистрирован. Войдите или используйте другой");
        }

        if (request.getRole() == Role.ADMIN) {
            throw new BadRequestException("Нельзя зарегистрироваться администратором");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setEnabled(true);

        User saved = userRepository.save(user);

        if (saved.getRole() == Role.CANDIDATE
                && candidateRepository.findByEmail(saved.getEmail()).isEmpty()) {
            Candidate candidate = new Candidate();
            candidate.setEmail(saved.getEmail());
            candidate.setUser(saved);
            candidateRepository.save(candidate);
        }

        RegisterResponse response = new RegisterResponse();
        response.setId(saved.getId());
        response.setEmail(saved.getEmail());
        response.setRole(saved.getRole());
        return response;
    }

    public RegisterResponse me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        RegisterResponse response = new RegisterResponse();
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setId(user.getId());
        return response;
    }

    public LoginResponse login(LoginRequest request) {
        String email = normalize(request.getEmail());
        // старые аккаунты могли сохраниться с заглавными буквами
        User user = userRepository.findByEmail(email)
                .or(() -> userRepository.findFirstByEmailIgnoreCaseOrderByIdAsc(email))
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        return response;
    }

    private static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
