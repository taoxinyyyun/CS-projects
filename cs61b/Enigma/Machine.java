package enigma;

import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Tess Tao
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _numPawls = pawls;
        _rotors = new Rotor[_numRotors];
        _allRotors = allRotors;
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _numPawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        for (int i = 0; i < rotors.length; i++) {
            for (Rotor aRotor : _allRotors) {
                String tName = aRotor.name();
                if (tName.equals(rotors[i])) {
                    if (i == 0 && !aRotor.reflecting()) {
                        throw new EnigmaException("not a reflector");
                    }
                    _rotors[i] = aRotor;
                }
            }
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        for (int i = 1; i < _rotors.length; i++) {
            char pos = setting.charAt(i - 1);
            _rotors[i].set(pos);
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugBoard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        boolean hasAdvance = false;
        boolean prevNotch = false;
        if (_rotors[_numRotors - 1].atNotch()) {
            prevNotch = true;
        }
        _rotors[_numRotors - 1].advance();
        hasAdvance = true;
        for (int i = _numRotors - 1; i > 0; i -= 1) {
            if (prevNotch & _rotors[i - 1].rotates()) {
                if (!_rotors[i - 1].atNotch()) {
                    prevNotch = false;
                }
                _rotors[i - 1].advance();
            } else if (!hasAdvance && _rotors[i].atNotch()
                    && _rotors[i - 1].rotates()) {
                if (!_rotors[i - 1].atNotch()) {
                    prevNotch = false;
                }
                _rotors[i - 1].advance();
                _rotors[i].advance();
            } else {
                hasAdvance = false;
                prevNotch = false;
            }
        }
        int output = _plugBoard.permute(c);
        for (int i = _numRotors - 1; i >= 0; i -= 1) {
            Rotor fRotor = _rotors[i];
            output = fRotor.convertForward(output);
        }
        for (int k = 1; k < _numRotors; k++) {
            Rotor bRotor = _rotors[k];
            output = bRotor.convertBackward(output);
        }
        output = _plugBoard.permute(output);
        return output;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        msg = msg.replaceAll(" ", "");
        int[] intArray = new int[msg.length()];
        for (int i = 0; i < msg.length(); i++) {
            int tInt = _alphabet.toInt(msg.charAt(i));
            intArray[i] = convert(tInt);
        }
        String codeString = "";
        for (int k = 0; k < intArray.length; k++) {
            String s = Character.toString(_alphabet.toChar(intArray[k]));
            codeString += s;
        }
        return codeString;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** The number of rotors that are available. */
    private int _numRotors;

    /** The number of pawls in the machine. */
    private int _numPawls;

    /** The list of rotors that are being used. */
    private Rotor[] _rotors;

    /** The collection of rotors that can be used. */
    private Collection<Rotor> _allRotors;

    /** The plugboard of this enigma machine. */
    private Permutation _plugBoard;

}
