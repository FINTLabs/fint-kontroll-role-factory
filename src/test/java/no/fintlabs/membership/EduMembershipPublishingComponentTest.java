package no.fintlabs.membership;

import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fintlabs.role.SkoleService;
import no.fintlabs.role.UndervisningsgruppeService;
import no.fintlabs.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EduMembershipPublishingComponentTest {

    @Mock
    private SkoleService skoleService;

    @Mock
    private UndervisningsgruppeService undervisningsgruppeService;

    @Mock
    private EduMembershipService eduMembershipService;

    @Mock
    private MembershipEntityProducerService membershipEntityProducerService;

    @Mock
    private UserService userService;

    @InjectMocks
    private EduMembershipPublishingComponent component;

    @Test
    void publishEduRoleMembershipssDeduplicatesUndervisningsgruppeMembershipsAndFavorsActiveStatus() {
        UndervisningsgruppeResource inactiveGroup = createUndervisningsgruppeResource("inactive");
        UndervisningsgruppeResource activeGroup = createUndervisningsgruppeResource("active");
        UndervisningsgruppeResource otherGroup = createUndervisningsgruppeResource("other");

        Membership inactiveDuplicateMembership = Membership.builder()
                .roleId(1L)
                .memberId(10L)
                .memberStatus("INACTIVE")
                .build();
        Membership activeDuplicateMembership = Membership.builder()
                .roleId(1L)
                .memberId(10L)
                .memberStatus("ACTIVE")
                .build();
        Membership otherMembership = Membership.builder()
                .roleId(1L)
                .memberId(20L)
                .memberStatus("INACTIVE")
                .build();

        when(userService.getNumberOfUsersInCache()).thenReturn(0L);
        when(skoleService.getAll()).thenReturn(List.<SkoleResource>of());
        when(undervisningsgruppeService.getAllValid()).thenReturn(List.of(inactiveGroup, activeGroup, otherGroup));
        when(eduMembershipService.createUndervisningsgruppeMembershipList(eq(inactiveGroup), any(Date.class), eq("ACTIVE")))
                .thenReturn(List.of(inactiveDuplicateMembership));
        when(eduMembershipService.createUndervisningsgruppeMembershipList(eq(activeGroup), any(Date.class), eq("ACTIVE")))
                .thenReturn(List.of(activeDuplicateMembership));
        when(eduMembershipService.createUndervisningsgruppeMembershipList(eq(otherGroup), any(Date.class), eq("ACTIVE")))
                .thenReturn(List.of(otherMembership));
        when(membershipEntityProducerService.publishChangedMemberships(any())).thenReturn(0);

        component.publishEduRoleMembershipss();

        ArgumentCaptor<List<Membership>> membershipsCaptor = ArgumentCaptor.forClass(List.class);
        verify(membershipEntityProducerService, times(2)).publishChangedMemberships(membershipsCaptor.capture());

        assertThat(membershipsCaptor.getAllValues().get(0)).isEmpty();
        assertThat(membershipsCaptor.getAllValues().get(1))
                .containsExactly(activeDuplicateMembership, otherMembership);
    }

    private UndervisningsgruppeResource createUndervisningsgruppeResource(String id) {
        UndervisningsgruppeResource resource = new UndervisningsgruppeResource();
        resource.setSystemId(createIdentifikator(id));
        return resource;
    }

    private Identifikator createIdentifikator(String value) {
        Identifikator identifikator = new Identifikator();
        identifikator.setIdentifikatorverdi(value);
        return identifikator;
    }
}
