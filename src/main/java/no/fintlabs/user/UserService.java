package no.fintlabs.user;

import no.fintlabs.cache.FintCache;
import no.fintlabs.member.Member;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final FintCache<String , User> userCache;
    public UserService(FintCache<String, User> userCache) {
        this.userCache = userCache;
    }
    public Optional<Member> getMember (String userId)
    {
        Optional<User> optionalUser =userCache.getOptional(userId);

        if (!optionalUser.isEmpty()) {
            return Optional.of(optionalUser.get().toMember());
        }
        return Optional.empty();
    }
    public Optional<User> getUser (String userId) {
        return userCache.getOptional(userId);
    }
    public Long getNumberOfUsersInCache() {
        return userCache.getNumberOfDistinctValues();
    }

    public List<User> getUsersWithUserType(String roleUserType) {

        return userCache.getAll()
                .stream()
                .filter(user -> user.getUserType() != null && user.getUserType().equalsIgnoreCase(roleUserType))
                .toList();
    }
}
