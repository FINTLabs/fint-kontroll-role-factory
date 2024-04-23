package no.fintlabs.role;

import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fintlabs.base.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SkoleServiceTests extends BaseTest {
    @Mock
    private ElevforholdService elevforholdService;
    @Mock GyldighetsperiodeService gyldighetsperiodeService;

    @InjectMocks
    private SkoleService skoleService;

    private Link studentRelationshipLink;
    private SkoleResource skoleResource;
    private ElevforholdResource studentRelationship;

    @BeforeEach
    public void SetUp() {
        String studentRelationshipId = "1";
        String schoolId = "school1";

        studentRelationshipLink = createLink(studentRelationshipId);
        studentRelationship = new ElevforholdResource();
        studentRelationship.setSystemId(createIdentifikator(studentRelationshipId));

        skoleResource = new SkoleResource();
        skoleResource.setSystemId(createIdentifikator(schoolId));
        skoleResource.addElevforhold(studentRelationshipLink);
    }

    @Test
    void givenOneValidStudentRelationship_getAllValidElevforhold_returnOneStudentRelationShip() {
        Periode validPeriod = getPeriod(10, 10);
        studentRelationship.setGyldighetsperiode(validPeriod);

        given(elevforholdService.getElevforhold(studentRelationshipLink)).willReturn(Optional.of(studentRelationship));
        given(gyldighetsperiodeService.isValid(validPeriod, getCurrentTime())).willReturn(true);

        List<ElevforholdResource> returnedStudentRelationShips = skoleService.getAllValidElevforhold(skoleResource, getCurrentTime() );

        assertThat(returnedStudentRelationShips.size()).isEqualTo(1);
    }
    @Test
    void givenOneInvalidStudentRelationship_getAllValidElevforhold_returnNoStudentRelationShip() {
        Periode invalidPeriod = getPeriod(10, 10);
        studentRelationship.setGyldighetsperiode(invalidPeriod);

        given(elevforholdService.getElevforhold(studentRelationshipLink)).willReturn(Optional.of(studentRelationship));
        given(gyldighetsperiodeService.isValid(invalidPeriod, getCurrentTime())).willReturn(false);

        List<ElevforholdResource> returnedStudentRelationShips = skoleService.getAllValidElevforhold(skoleResource, getCurrentTime() );

        assertThat(returnedStudentRelationShips.size()).isEqualTo(0);
    }
}