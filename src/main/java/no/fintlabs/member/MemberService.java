package no.fintlabs.member;

import no.fintlabs.cache.FintCache;
import no.fintlabs.role.SimpleMember;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemberService {
    private final FintCache<String , Member> memberCache;

    public MemberService(FintCache<String, Member> memberCache) {
        this.memberCache = memberCache;
    }

    public Optional<Member> getMember (String resourceId)
    {
        return memberCache.getOptional(resourceId);
    }
}
