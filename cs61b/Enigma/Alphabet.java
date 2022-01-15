package enigma;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Tess Tao
 */
class Alphabet {

    /** All the characters of this alphabet. */
    private String _characters;

    /** A new alphabet containing CHARS.  Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        if (chars.contains(" ") || chars.contains("(")
                || chars.contains("*") || chars.contains(")")) {
            throw new EnigmaException("Invalid Alphabet Composition");
        }
        _characters = chars;
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _characters.length();
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        if (_characters == null) {
            return 'A' <= ch && ch <= 'Z';
        } else {
            for (int i = 0; i < _characters.length(); i++) {
                if (_characters.charAt(i) == ch) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        if (_characters == null) {
            return (char) ('A' + index);
        } else {
            return _characters.charAt(index);
        }
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        if (_characters == null) {
            return ch - 'A';
        } else {
            int retInt = _characters.indexOf(ch);
            if (retInt == -1) {
                throw new EnigmaException("Not in alphabet");
            } else {
                return retInt;
            }
        }
    }

}
