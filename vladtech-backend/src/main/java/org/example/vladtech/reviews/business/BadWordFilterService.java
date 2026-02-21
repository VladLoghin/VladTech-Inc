package org.example.vladtech.reviews.business;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class BadWordFilterService {

    private static final Map<Character, Character> CHAR_SUBSTITUTIONS = Map.ofEntries(
            Map.entry('$', 's'),
            Map.entry('@', 'a'),
            Map.entry('0', 'o'),
            Map.entry('1', 'i'),
            Map.entry('3', 'e'),
            Map.entry('4', 'a'),
            Map.entry('5', 's'),
            Map.entry('7', 't'),
            Map.entry('8', 'b'),
            Map.entry('!', 'i'),
            Map.entry('+', 't'),
            Map.entry('(', 'c'),
            Map.entry('|', 'l'),
            Map.entry('*', '*')
    );

    private static final Set<String> BAD_WORDS = Set.of(
            "fuck", "shit", "ass", "bitch", "damn", "bastard",
    "dick", "piss", "crap", "cunt", "asshole",
    "motherfucker", "fucker", "bullshit", "dumbass",
    "jackass", "nigger", "nigga", "faggot", "slut",
    "whore", "retard", "cock", "pussy", "poop", "poopy", "wanker", "a-hole", "arsehole", 
    "cockeye", "douche", "douchebag",
    "douchewaffle", "douchecanoe", "douchelord", "douchemonster", "asshat", "niggerboy", "niggergirl",
    "niggerwoman", "niggerman", "niggerchild", "tard", "niggerlover", "slaveboy", "slavegirl", "slaveman",
    "slavechild", "slavelover", "hook-nosed", "porstitute", "creampie", "rimjob", "blowjob", "handjob", 
    "cunnilingus", "anilingus", "fisting",
    "cretinous", "imbecile", "idiot", "moron", "cuntface", "cuntlaper", "crotle", "dickhead", "cockface",
    "cocklaper", "dicklaper", "dickface", "pussyface", "curry muncher", "sanddweller", "camel humper", "dipshit",
    "dyke", "fap", "fapmaster", "fapwaffle", "fapcanoe", "faplord", "fapmonster", "fuckwit", "fucktwat", "hoe",
    "incest", "jizz", "arse", "mimbo", "manwhore", "nuts", "orgasm", "pissprat", "prick", "punani", "queer", "rimjobber", "scrote", "sperm",
    "seks", "sex", "buttsex", "titty", "shitfaced", "tits", "nipples", "harlot", "pegging", "suck", "sucker", "suckme", "suckmy", "vagina", "penis",
    "penishead", "gay", "lesbian", "trans", "boob", "boobies", "manboobs",

    "Tabarnak", "Câlice", "Ciboire", "Hostie", "Ostie", "Sacrament", "Sainte-Câlice", "Sainte-Hostie", "Sainte-Sacrament",
    "putain", "merde", "con", "connard", "salop", "enculé", "bordel", "ta gueule", "nique ta mère", "nique sa mère", "nique vos mères", "nique le front national"
    );

    public boolean containsBadWords(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String lowerText = text.toLowerCase();
        String normalizedText = normalizeText(lowerText);

        for (String word : BAD_WORDS) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b");
            if (pattern.matcher(lowerText).find() || pattern.matcher(normalizedText).find()) {
                return true;
            }
        }
        return false;
    }

    private String normalizeText(String text) {
        StringBuilder normalized = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (CHAR_SUBSTITUTIONS.containsKey(c)) {
                char replacement = CHAR_SUBSTITUTIONS.get(c);
                if (replacement != '*') {
                    normalized.append(replacement);
                }
            } else {
                normalized.append(c);
            }
        }
        return normalized.toString();
    }
}
