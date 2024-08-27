package no.fintlabs.membership;

import java.util.Date;

public record MembershipStatus (String status, Date statusChanged) {}