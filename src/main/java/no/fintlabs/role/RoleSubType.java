package no.fintlabs.role;
public enum RoleSubType {
    ORGANISASJONSELEMENT("organisasjonselement"),
    ORGANISASJONSELEMENT_AGGREGERT("organisasjonselement aggregert"),
    BASISGRUPPE("basisgruppe"),
    UNDERVISNINGSGRUPPE("undervisningsgruppe"),
    SKOLEGRUPPE("skolegruppe"),
    BRUKERTYPEGRUPPE("brukertypegruppe");

    private final String roleSubType;

    RoleSubType(String roleSubType) {
        this.roleSubType = roleSubType;
    }

    public String getRoleSubType() {
        return roleSubType;
    }
}
