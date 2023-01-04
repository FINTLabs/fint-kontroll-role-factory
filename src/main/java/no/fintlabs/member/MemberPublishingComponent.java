package no.fintlabs.member;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.user.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.Long.parseLong;

@Slf4j
@Component

public class MemberPublishingComponent {

    private final FintCache<String, User> userCache;
    private final MemberEntityProducerService memberEntityProducerService;

    public MemberPublishingComponent(
            FintCache<String, User> userCache,
            MemberEntityProducerService memberEntityProducerService
    ) {
        this.userCache = userCache;
        this.memberEntityProducerService = memberEntityProducerService;
    }

    @Scheduled(
            //initialDelay = 20000L,
            //fixedDelay = 20000L
            initialDelayString = "${fint.kontroll.member.publishing.initial-delay}",
            fixedDelayString = "${fint.kontroll.member.publishing.fixed-delay}"
    )
    public void PublishMembers() {
        List<Member> memberList= userCache.getAllDistinct()
                .stream()
                .map(user -> createMember(user))
                .toList();

        List<Member> publishedMembers = memberEntityProducerService.publishChangedMembers(memberList);

        log.info("Published {} of {} valid members", publishedMembers.size(), memberList.size());
        log.debug("Ids of published members: {}",
                publishedMembers.stream()
                        .map(Member::getResourceId)
                        .map(href -> href.substring(href.lastIndexOf("/") + 1))
                        .toList()
        );
    }

    private Member createMember(User user) {

        String resourceId = user.getResourceId();
        Long id = parseLong(resourceId.substring(resourceId.lastIndexOf("/") + 1));
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String userType = user.getUserType();
        String userName = user.getUserName();

        return Member
                .builder()
                .id(id)
                .resourceId(resourceId)
                .firstName(firstName)
                .lastName(lastName)
                .userName(userName)
                .userType(userType)
                .build();
    }
}
