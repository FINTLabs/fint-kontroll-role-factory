package no.fintlabs.role;
public enum RoleType {
    ELEV("elev"), LARER("larer"), ANSATT("ansatt");

    private final String roleType;

    RoleType(String roleType) {
        this.roleType = roleType;
    }

    public String getRoleType() {
        return roleType;
    }
}
