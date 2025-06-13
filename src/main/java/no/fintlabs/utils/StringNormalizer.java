package no.fintlabs.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringNormalizer {
    private static final Pattern pattern = Pattern.compile("[^\\p{ASCII}]");

    public static String normalize(String text) {
        String tmpText = text
                .toLowerCase()
                .replace("ø","o")
                .replace("æ", "a")
                .replace('/','-')
                .replace(' ','-');
        return Normalizer
                .normalize(tmpText, Normalizer.Form.NFD)
                .replaceAll(pattern.pattern(), "");
    }
    public static String identifikatorNameToLowerCase(String path) {
        String lowerCasePathWithoutValue = path.substring(0, path.lastIndexOf('/')).toLowerCase();
        String value = path.substring(path.lastIndexOf('/'));
        return lowerCasePathWithoutValue + value;
    }
}
