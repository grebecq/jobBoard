package fedoseev.jobboard.service;


import fedoseev.jobboard.dto.request.CandidateRequest;
import fedoseev.jobboard.dto.response.CandidateResponse;
import fedoseev.jobboard.entity.Candidate;
import fedoseev.jobboard.exception.DuplicateResourceException;
import fedoseev.jobboard.mapper.CandidateMapper;
import fedoseev.jobboard.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;

    private final CandidateMapper candidateMapper;

    public CandidateResponse createdCandidate(CandidateRequest request){
        if(candidateRepository.findByEmail(request.getEmail()).isPresent()){
            throw new DuplicateResourceException("Candidate already exists :" + request.getEmail());
        }
        Candidate saved = candidateRepository.save(candidateMapper.toEntity(request));
        return candidateMapper.toResponse(saved);
    }


}
