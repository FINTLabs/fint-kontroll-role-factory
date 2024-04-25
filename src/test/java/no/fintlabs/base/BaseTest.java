package no.fintlabs.base;

import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fint.model.resource.Link;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public abstract class BaseTest {

    protected Date getCurrentTime() {
        return  Date.from((LocalDate.now().atStartOfDay(ZoneId.of("Z")).toInstant()));
    }

    protected Periode getPeriod(long daysbefore, long daysafter) {
        Periode periode = new Periode();
        Date startDate = Date.from((LocalDate.now().minusDays(daysbefore).atStartOfDay(ZoneId.of("Z")).toInstant()));
        Date sluttDate = Date.from(LocalDate.now().plusDays(daysafter).atStartOfDay(ZoneId.of("Z")).toInstant());
        periode.setStart(startDate);
        periode.setSlutt(sluttDate);
        return periode;
    }
    protected Link createLink(String value) {
        return new Link("/link/" + value);
    }

    protected Identifikator createIdentifikator(String identifikatorveridi) {
        Identifikator id= new Identifikator();
        id.setIdentifikatorverdi(identifikatorveridi);
        return id;
    }
    protected String createMembershipId(String groupId, String memberId) {
        return groupId + "_" + memberId;
    }
}
