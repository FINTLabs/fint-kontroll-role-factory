package no.fintlabs.role;

import no.fintlabs.member.Member;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class Role {
    private Long id;
    private String resourceId;
    private String roleId;
    private String roleStatus;
    private Date roleStatusChanged;
    private String roleName;
    private String roleDescription;
    private String roleType;
    private String roleSubType;
    private Boolean aggregatedRole;
    private String roleSource;
    private String organisationUnitId;
    private String organisationUnitName;
    private RoleRef parentRoleId;
    //private Integer noOfMembers;
    //private List<Member> members;
    private List<RoleRef> childrenRoleIds;
    private boolean isSchoolRole;
}
