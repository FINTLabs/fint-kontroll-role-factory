package no.fintlabs.membership;

import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.ElevResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppemedlemskapResource;
import no.fintlabs.role.ElevforholdService;
import no.fintlabs.role.RoleCatalogRole;
import no.fintlabs.role.RoleService;
import no.fintlabs.role.RoleType;
import no.fintlabs.role.SkoleService;
import no.fintlabs.role.UndervisningsgruppeService;
import no.fintlabs.user.User;
import no.fintlabs.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EduMembershipServiceTest {

    @Mock
    private SkoleService skoleService;

    @Mock
    private UndervisningsgruppeService undervisningsgruppeService;

    @Mock
    private ElevforholdService elevforholdService;

    @Mock
    private UserService userService;

    @Mock
    private MembershipService membershipService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private EduMembershipService eduMembershipService;

    @Test
    void givenGroupMembershipWithoutGyldighetsperiode_createUndervisningsgruppeMembershipList_shouldCreateMembershipWithNullDates() {
        UndervisningsgruppeResource studyGroup = createStudyGroup();
        UndervisningsgruppemedlemskapResource groupMembership = createGroupMembership();
        ElevforholdResource elevforhold = createElevforhold();
        ElevResource elev = createElev();
        User user = User.builder().id(123L).status("ACTIVE").build();
        Membership membership = Membership.builder()
                .roleId(456L)
                .memberId(123L)
                .memberStatus("ACTIVE")
                .build();
        RoleCatalogRole roleCatalogRole = new RoleCatalogRole();
        roleCatalogRole.setId(456L);
        roleCatalogRole.setRoleStatus("ACTIVE");

        when(roleService.createUndervisningsgruppeRoleId(studyGroup, RoleType.ELEV.getRoleType())).thenReturn("elev@123-1A");
        when(roleService.getRoleCatalogRole("elev@123-1A")).thenReturn(Optional.of(roleCatalogRole));
        when(undervisningsgruppeService.getAllGruppemedlemskap(studyGroup)).thenReturn(List.of(groupMembership));
        when(elevforholdService.getElevforhold(groupMembership)).thenReturn(Optional.of(elevforhold));
        when(elevforholdService.getElev(elevforhold)).thenReturn(Optional.of(elev));
        when(userService.getUser("student-1")).thenReturn(Optional.of(user));
        when(membershipService.createMembership(eq(roleCatalogRole), eq(user), eq("ACTIVE"), eq(null), eq(null))).thenReturn(membership);

        List<Membership> result = eduMembershipService.createUndervisningsgruppeMembershipList(studyGroup, new Date());

        assertThat(result).containsExactly(membership);
    }

    private UndervisningsgruppeResource createStudyGroup() {
        UndervisningsgruppeResource studyGroup = new UndervisningsgruppeResource();
        studyGroup.setSystemId(createIdentifikator("studygroup-1"));
        studyGroup.setNavn("1A");
        studyGroup.addSelf(new Link("https://example.com/undervisningsgruppe/studygroup-1"));
        return studyGroup;
    }

    private UndervisningsgruppemedlemskapResource createGroupMembership() {
        UndervisningsgruppemedlemskapResource groupMembership = new UndervisningsgruppemedlemskapResource();
        groupMembership.setSystemId(createIdentifikator("groupmembership-1"));
        return groupMembership;
    }

    private ElevforholdResource createElevforhold() {
        ElevforholdResource elevforhold = new ElevforholdResource();
        elevforhold.setSystemId(createIdentifikator("elevforhold-1"));
        elevforhold.setGyldighetsperiode(createPeriod());
        return elevforhold;
    }

    private ElevResource createElev() {
        ElevResource elev = new ElevResource();
        elev.setElevnummer(createIdentifikator("student-1"));
        return elev;
    }

    private Periode createPeriod() {
        Periode periode = new Periode();
        periode.setStart(new Date(0));
        return periode;
    }

    private Identifikator createIdentifikator(String value) {
        Identifikator identifikator = new Identifikator();
        identifikator.setIdentifikatorverdi(value);
        return identifikator;
    }
}
