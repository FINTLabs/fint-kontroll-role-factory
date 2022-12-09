package no.fintlabs.role;
public enum RoleType {
    ELEVGRUPPE("elevgruppe"), LARERGRUPPE("");

    private final String roleType;

    RoleType(String roleType) {
        this.roleType = roleType;
    }

    public String getRoleType() {
        return roleType;
    }
}
