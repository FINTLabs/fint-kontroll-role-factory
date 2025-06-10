package no.fintlabs.membership;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.role.Role;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserTypeMembershipService {

    //TODO: Implement this service to create memberships based on user types.

    public List<Membership> creatUserTypeMembershipList(Role userTypeRole) {
        return List.of();
    }

}
