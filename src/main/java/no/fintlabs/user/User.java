package no.fintlabs.user;

import lombok.Builder;
import lombok.Data;
import no.fintlabs.member.Member;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class User {

    private Long id;
    private String resourceId;
    private String firstName;
    private String lastName;
    private String userType;
    private String userName;
    private UUID identityProviderUserObjectId;
    private List<RoleRefs> roleRefs;
    private String mobilePhone;
    private String email;
    private  String managerRef;

    public Member toMember() {
        return Member
                .builder()
                .id(id)
                .resourceId(resourceId)
                .firstName(firstName)
                .lastName(lastName)
                .userType(userType)
                .userName(userName)
                .identityProviderUserObjectId(identityProviderUserObjectId)
                .build();
    }
}

