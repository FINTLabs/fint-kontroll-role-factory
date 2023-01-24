package no.fintlabs.role;

import no.fintlabs.member.Member;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Role {
    private Long id;
    private String resourceId;
    private String roleId;
    private String roleName;
    private String roleDescription;
    private String roleType;
    private String roleSubType;
    private String aggregatedRole;
    private String roleSource;
    private RoleRef parentRoleId;
    private List<Member> members;
    private List<RoleRef> childrenRoleIds;
}
