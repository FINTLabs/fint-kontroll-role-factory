package no.fintlabs.member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
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
    private String memberStatus;
    private Date memberStatusChanged;

    public Membership toMemberShip(Long roleId) {
        return Membership.builder()
                .memberId(id)
                //.roleId(roleId)
                //.memberStatusChanged(memberStatusChanged)
                .memberStatus(memberStatus)
                .build();
    }
    public Membership toMemberShip() {
        return Membership.builder()
                .memberId(id)
                //.roleId(roleId)
                //.memberStatusChanged(memberStatusChanged)
                .memberStatus(memberStatus)
                .build();
    }
}
