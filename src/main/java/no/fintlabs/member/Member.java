package no.fintlabs.member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class Member {
    private Long id;
    private String resourceId;
    private String firstName;
    private  String lastName;
    private UUID identityProviderUserObjectId;
    private String userName;
    private  String userType;
}
