package enigma;
import java.util.ArrayList;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Tess
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = new ArrayList<String>();
        if (cycles.length() != 0) {
            String[] splitCycles = cycles.split("\\s+");
            for (String i : splitCycles) {
                if (!i.startsWith("(")) {
                    throw new EnigmaException("Invalid Input");
                } else {
                    addCycle(i);
                }
            }
        }
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        if (!cycle.endsWith(")")) {
            throw new EnigmaException("Invalid Input");
        } else {
            int e = cycle.indexOf(")");
            while (e != cycle.length() - 1) {
                String subCycle = cycle.substring(1, e);
                _cycles.add(subCycle);
                cycle = cycle.substring(e + 1);
                e = cycle.indexOf(")");
            }
            String subCycle = cycle.substring(1, e);
            _cycles.add(subCycle);
        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        int p1 = wrap(p);
        char input = _alphabet.toChar(p1);
        char output = input;
        for (String i : _cycles) {
            for (int k = 0; k < i.length(); k++) {
                if (i.charAt(k) == input && k != i.length() - 1) {
                    output = i.charAt(k + 1);
                } else if (k == i.length() - 1 && i.charAt(k) == input) {
                    output = i.charAt(0);
                }
            }
        }
        return _alphabet.toInt(output);
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        int c1 = wrap(c);
        char input = _alphabet.toChar(c1);
        char output = input;
        for (String i : _cycles) {
            for (int k = 0; k < i.length(); k++) {
                if (i.charAt(k) == input && k != 0) {
                    output = i.charAt(k - 1);
                } else if (i.charAt(0) == input) {
                    output = i.charAt(i.length() - 1);
                }
            }
        }
        return _alphabet.toInt(output);
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int p1 = _alphabet.toInt(p);
        int output = permute(p1);
        return _alphabet.toChar(output);
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        int c1 = _alphabet.toInt(c);
        int output = invert(c1);
        return _alphabet.toChar(output);
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        int size = _alphabet.size();
        for (int k = 0; k < size; k++) {
            int p = permute(k);
            if (p == k) {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** A permutation in the cycle notation. */
    private ArrayList<String> _cycles;

}
