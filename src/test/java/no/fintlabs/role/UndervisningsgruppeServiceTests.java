package no.fintlabs.role;

import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppemedlemskapResource;
import no.fintlabs.base.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class UndervisningsgruppeServiceTests extends BaseTest {

    @Mock
    private  UndervisningsgruppemedlemskapService undervisningsgruppemedlemskapService;
    @Mock
    private ElevforholdService elevforholdService;
    @Mock GyldighetsperiodeService gyldighetsperiodeService;

    @InjectMocks
    private UndervisningsgruppeService undervisningsgruppeService;
    private Link groupMembershipLink;
    private UndervisningsgruppeResource studyGroup;

    private UndervisningsgruppemedlemskapResource groupMembership;
    private ElevforholdResource studentRelationship;

    @BeforeEach
    public void SetUp() {
        String studentRelationshipId = "1";
        String studyGroupId = "studygroup1";
        String groupMembershipId = createMembershipId(studyGroupId, studentRelationshipId);

        Link studentRelationshipLink = createLink(studentRelationshipId);
        groupMembershipLink = createLink(groupMembershipId);

        studentRelationship = new ElevforholdResource();
        studentRelationship.setSystemId(createIdentifikator(studentRelationshipId));

        studyGroup = new UndervisningsgruppeResource();
        studyGroup.setSystemId(createIdentifikator(studyGroupId));
        studyGroup.addGruppemedlemskap(groupMembershipLink);

        groupMembership = new UndervisningsgruppemedlemskapResource();
        groupMembership.setSystemId(createIdentifikator(groupMembershipId));
        groupMembership.addElevforhold(studentRelationshipLink);
    }
    @Test
    void givenOneValidPeriodMembershipInGroup_getValidAllElevforhold_returnOneStudentRelationShip() {
        Periode validPeriod = getPeriod(10, 10);
        groupMembership.setGyldighetsperiode(validPeriod);

        given(undervisningsgruppemedlemskapService.getUndervisningsgruppemedlemskap(groupMembershipLink)).willReturn(Optional.of(groupMembership));
        given(elevforholdService.getElevforhold(groupMembership)).willReturn(Optional.of(studentRelationship));
        //given(gyldighetsperiodeService.isValid(validPeriod, getCurrentTime(), 0)).willReturn(true);

        List<ElevforholdResource> returnedStudentRelationShips = undervisningsgruppeService.getValidAllElevforhold(studyGroup, getCurrentTime() );

        assertThat(returnedStudentRelationShips.size()).isEqualTo(1);
    }
    @Test
    void givenOneInValidPeriodMembershipInGroup_getValidAllElevforhold_returnNoStudentRelationShip() {
        Periode invalidPeriod = getPeriod(10, -2);
        groupMembership.setGyldighetsperiode(invalidPeriod);

        given(undervisningsgruppemedlemskapService.getUndervisningsgruppemedlemskap(groupMembershipLink)).willReturn(Optional.of(groupMembership));
        //given(gyldighetsperiodeService.isValid(invalidPeriod, getCurrentTime(), 0)).willReturn(false);

        List<ElevforholdResource> returnedStudentRelationShips = undervisningsgruppeService.getValidAllElevforhold(studyGroup, getCurrentTime() );

        assertThat(returnedStudentRelationShips.size()).isEqualTo(0);
    }
    @Test
    void givenOneNoPeriodMembershipInGroup_getValidAllElevforhold_returnOneStudentRelationShip() {
        given(undervisningsgruppemedlemskapService.getUndervisningsgruppemedlemskap(groupMembershipLink))
                .willReturn(Optional.of(groupMembership));
        given(elevforholdService.getElevforhold(groupMembership)).willReturn(Optional.of(studentRelationship));

        List<ElevforholdResource> returnedStudentRelationShips = undervisningsgruppeService.getValidAllElevforhold(studyGroup, getCurrentTime() );

        assertThat(returnedStudentRelationShips.size()).isEqualTo(1);
    }
}