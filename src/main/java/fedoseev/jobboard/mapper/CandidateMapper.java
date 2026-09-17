package fedoseev.jobboard.mapper;

import fedoseev.jobboard.dto.request.CandidateRequest;
import fedoseev.jobboard.dto.response.CandidateResponse;
import fedoseev.jobboard.entity.Candidate;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CandidateMapper {
    Candidate toEntity(CandidateRequest request);

    CandidateResponse toResponse(Candidate candidate);
}
