package no.fintlabs.role;

import java.util.Date;
import java.util.List;

public class RoleDTO {
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
    private Integer noOfMembers;
    private List<RoleRef> childrenRoleIds;
}
