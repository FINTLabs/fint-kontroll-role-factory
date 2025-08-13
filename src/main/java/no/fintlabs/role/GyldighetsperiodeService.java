package no.fintlabs.role;

import no.fint.model.felles.kompleksedatatyper.Periode;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class GyldighetsperiodeService {

    public static class NullPeriodeException extends RuntimeException {
    }
    public static class NullPeriodeStartDatoException extends RuntimeException {
    }

    public boolean isValid(Periode gyldighetsperiode, Date currentTime) {
        if (gyldighetsperiode == null) {
            throw new NullPeriodeException();
        }
        if (gyldighetsperiode.getStart() == null) {
            throw new NullPeriodeStartDatoException();
        }
        return currentTime.after(gyldighetsperiode.getStart())
                && isEndValid(gyldighetsperiode.getSlutt(), currentTime);
    }

    public boolean isValid(Periode gyldighetsperiode, Date currentTime, int daysBeforeStart) {
        if (gyldighetsperiode == null) {
            throw new NullPeriodeException();
        }
        if (gyldighetsperiode.getStart() == null) {
            throw new NullPeriodeStartDatoException();
        }
        Date startInit = gyldighetsperiode.getStart();
        Date start = getStartDate(startInit, daysBeforeStart);

        return currentTime.after(start)
                && isEndValid(gyldighetsperiode.getSlutt(), currentTime);
    }

    private boolean isEndValid(Date end, Date currentTime) {
        return end == null || currentTime.before(end);
    }

    public Date getStartDate(Date date, int daysBeforeStart) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -daysBeforeStart);

        return calendar.getTime();
    }
}