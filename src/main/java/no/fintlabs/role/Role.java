package no.fintlabs.role;

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
    private String aggregatedRole;
    private String roleSource;
    private RoleRef parentRoleId;
    private List<MemberRef> memberIds;
    private List<RoleRef> childrenRoleIds;
}
