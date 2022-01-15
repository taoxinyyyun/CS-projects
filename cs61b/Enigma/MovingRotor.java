package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Tess
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _perm = perm;
        _notches = notches;
    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    boolean atNotch() {
        char position = _perm.alphabet().toChar(setting());
        for (int k = 0; k < _notches.length(); k++) {
            if (_notches.charAt(k) == position) {
                return true;
            }
        }
        return false;
    }

    @Override
    void advance() {
        int curr = setting();
        if (curr == alphabet().size() - 1) {
            set(0);
        } else {
            set(curr + 1);
        }
    }

    /** This machine's notches. */
    private String _notches;

    /** This machine's permutation. */
    private Permutation _perm;


}
