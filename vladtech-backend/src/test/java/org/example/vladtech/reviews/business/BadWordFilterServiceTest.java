package org.example.vladtech.reviews.business;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BadWordFilterServiceTest {

    private BadWordFilterService badWordFilterService;

    @BeforeEach
    void setUp() {
        badWordFilterService = new BadWordFilterService();
    }

    @Test
    void containsBadWords_withBadWord_returnsTrue() {
        assertTrue(badWordFilterService.containsBadWords("This is shit"));
    }

    @Test
    void containsBadWords_withBadWordCaseInsensitive_returnsTrue() {
        assertTrue(badWordFilterService.containsBadWords("This is SHIT"));
    }

    @Test
    void containsBadWords_withBadWordMixedCase_returnsTrue() {
        assertTrue(badWordFilterService.containsBadWords("What the Fuck"));
    }

    @Test
    void containsBadWords_withCleanText_returnsFalse() {
        assertFalse(badWordFilterService.containsBadWords("Great service, very professional!"));
    }

    @Test
    void containsBadWords_withNullText_returnsFalse() {
        assertFalse(badWordFilterService.containsBadWords(null));
    }

    @Test
    void containsBadWords_withEmptyText_returnsFalse() {
        assertFalse(badWordFilterService.containsBadWords(""));
    }

    @Test
    void containsBadWords_withBlankText_returnsFalse() {
        assertFalse(badWordFilterService.containsBadWords("   "));
    }

    @Test
    void containsBadWords_withBadWordAtStart_returnsTrue() {
        assertTrue(badWordFilterService.containsBadWords("damn this is bad"));
    }

    @Test
    void containsBadWords_withBadWordAtEnd_returnsTrue() {
        assertTrue(badWordFilterService.containsBadWords("this is damn"));
    }

    @Test
    void containsBadWords_withMultipleBadWords_returnsTrue() {
        assertTrue(badWordFilterService.containsBadWords("fuck this shit"));
    }

    @Test
    void containsBadWords_withBadWordAsSubstring_returnsFalse() {
        assertFalse(badWordFilterService.containsBadWords("classroom assignment"));
    }
}
