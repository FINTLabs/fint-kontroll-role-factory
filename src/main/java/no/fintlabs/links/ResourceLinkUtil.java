package no.fintlabs.links;

import no.fint.model.resource.FintLinks;
import no.fint.model.resource.Link;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResourceLinkUtil {

    public static String getFirstSelfLink(FintLinks resource) {
        return resource.getSelfLinks()
                .stream()
                .findFirst()
                .orElseThrow(() -> NoSuchLinkException.noSelfLink(resource))
                .getHref();
    }

    public static List<String> getSelfLinks(FintLinks resource) {
        return resource.getSelfLinks()
                .stream()
                .map(Link::getHref)
                .collect(Collectors.toList());
    }
    public static String getSelfLinkOfKind(FintLinks resource, String kind) {
        return getSelfLinks(resource)
                .stream()
                .filter(href -> href.contains(kind))
                .findFirst()
                .orElseThrow(() -> NoSuchLinkException.noSelfLinkOfKind(resource, kind));
    }

    public static String getFirstLink(Supplier<List<Link>> linkProducer, FintLinks resource, String linkedResourceName) {
        return Optional.ofNullable(linkProducer.get())
                .map(Collection::stream)
                .flatMap(Stream::findFirst)
                .map(Link::getHref)
                .orElseThrow(() -> NoSuchLinkException.noLink(resource, linkedResourceName));
    }
    public static Optional<String> getOptionalFirstLink(Supplier<List<Link>> linkProducer) {
        return Optional.ofNullable(linkProducer.get())
                .map(Collection::stream)
                .flatMap(Stream::findFirst)
                .map(Link::getHref)
                .map(ResourceLinkUtil::systemIdToLowerCase);
    }
    public static Function<Link, String> linkToString = link -> Optional.ofNullable(link.getHref())
            .map(String::toLowerCase).orElse(null);

    public static String systemIdToLowerCase(String path) {
        return path.replace("systemId", "systemid");
    }

    public static String organisasjonsIdToLowerCase(String path) {
        return path.replace("organisasjonsId", "organisasjonsid");
    }

}