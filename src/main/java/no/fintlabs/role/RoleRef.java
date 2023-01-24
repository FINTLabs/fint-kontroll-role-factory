package no.fintlabs.role;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class RoleRef {

    private String roleRef;
    RoleRef(String roleref)
    {
        this.roleRef = roleref;
    }
}
