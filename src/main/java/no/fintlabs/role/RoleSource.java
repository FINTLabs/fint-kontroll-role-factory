package no.fintlabs.role;
public enum RoleSource {
    FINT("fint"), PORTAL("portal");

    private final String roleSource;

    RoleSource(String roleSource) {
        this.roleSource= roleSource;
    }

    public String getRoleSource() {
        return roleSource;
    }
}