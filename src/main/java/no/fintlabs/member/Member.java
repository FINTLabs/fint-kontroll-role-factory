package no.fintlabs.member;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Member {
    private Long id;
    private String resourceId;
    private String firstName;
    private  String lastName;
    private String userId;
    private String userName;
    private  String userType;
}
