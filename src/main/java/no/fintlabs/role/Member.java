package no.fintlabs.role;

import lombok.Data;

@Data
public class Member {

    private Long id;

    public Member(){}

    public Member (Long id)
    {
        this.id = id;
    }
}
