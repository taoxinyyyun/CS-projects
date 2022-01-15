package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Tess
 */
public class PermutationTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that perm has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }

    @Test
    public void testInvertChar() {
        Permutation p = new Permutation("(BACD)",
                new Alphabet("ABCD"));
        assertEquals('B', p.invert('A'));
        assertEquals('D', p.invert('B'));
        assertEquals('C', p.invert('D'));
    }

    @Test
    public void testPermuteInt() {
        Permutation p = new Permutation("(ADORE) (BIT)",
                new Alphabet("ABDEIORT"));
        assertEquals(0, p.permute(3));
        assertEquals(7, p.permute(4));
        assertEquals(1, p.permute(7));
        assertEquals(1, p.permute(-1));
        assertEquals(4, p.permute(9));
    }

    @Test
    public void testInvertInt() {
        Permutation p = new Permutation("(ADORE) (BIT)",
                new Alphabet("ABDEIORT"));
        assertEquals(3, p.invert(0));
        assertEquals(7, p.invert(1));
        assertEquals(0, p.invert(2));
        assertEquals(7, p.invert(9));
        assertEquals(5, p.invert(-2));
    }

    @Test
    public void testPermuteChar() {
        Permutation p = new Permutation("(EGI) (FKL)",
                new Alphabet("EFGHIJKL"));
        assertEquals('F', p.permute('L'));
        assertEquals('H', p.permute('H'));
        assertEquals('I', p.permute('G'));
    }

    @Test
    public void testDerangement() {
        Permutation p = new Permutation("(ADORE) (BIT)",
                new Alphabet("ABDEIORT"));
        Permutation q = new Permutation("(EGI) (FKL)",
                new Alphabet("EFGHIJKL"));
        assertTrue(p.derangement());
        assertFalse(q.derangement());
    }

    @Test(expected = EnigmaException.class)
    public void testNotInAlphabet() {
        Permutation p = new Permutation("(ADORE) (BIT)",
                new Alphabet("ABDEIORT"));
        p.invert('F');
    }


}

