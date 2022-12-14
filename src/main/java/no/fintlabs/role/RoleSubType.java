package no.fintlabs.role;
public enum RoleSubType {
    BASISGRUPPE("basisgruppe"), UNDERVISNINGSGRUPPE("undervisningsgruppe"), SKOLEGRUPPE("skolegruppe");

    private final String roleSubType;

    RoleSubType(String roleSubType) {
        this.roleSubType = roleSubType;
    }

    public String getRoleSubType() {
        return roleSubType;
    }
}
