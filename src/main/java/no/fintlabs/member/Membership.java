package no.fintlabs.member;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class Membership {
    private Long roleId;
    private Long memberId;
    private String memberStatus;
    private Date memberStatusChanged;
}
