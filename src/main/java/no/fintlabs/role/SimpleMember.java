package no.fintlabs.role;

import lombok.Data;

@Data
public class SimpleMember {

    private Long id;
    public SimpleMember(){}

    public SimpleMember(Long id)
    {
        this.id = id;
    }
}
