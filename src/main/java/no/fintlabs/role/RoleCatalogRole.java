package no.fintlabs.role;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class RoleCatalogRole {
    protected Long id;
    protected String roleId;
    protected String roleStatus;
    protected Date roleStatusChanged;
}
