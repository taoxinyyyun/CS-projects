package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Tess Tao
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine machine = readConfig();
        String nextLine = _input.nextLine();
        if (nextLine.charAt(0) != '*') {
            throw error("Wrong input");
        }
        checkRotors(nextLine);
        String[] settings = nextLine.substring(2).split(" ");
        String[] rotors = new String[numRotors];
        System.arraycopy(settings, 0, rotors, 0, numRotors);
        machine.insertRotors(rotors);
        _setting = settings[numRotors];
        setUp(machine, _setting);
        if (nextLine.contains("(")) {
            int begin = nextLine.indexOf("(");
            String pCycle = nextLine.substring(begin);
            Permutation plugPerm = new Permutation(pCycle, _alphabet);
            machine.setPlugboard(plugPerm);
        } else {
            String pCycle = "";
            Permutation plugPerm = new Permutation(pCycle, _alphabet);
            machine.setPlugboard(plugPerm);
        }
        while (_input.hasNextLine()) {
            String msg = _input.nextLine();
            if (msg.length() == 0) {
                if (_input.hasNextLine()) {
                    msg = _input.nextLine();
                }
                if (msg.length() != 0) {
                    _output.println();
                }
            }
            if (msg.length() != 0 && msg.charAt(0) == '*') {
                checkRotors(msg);
                String[] setting2 = msg.substring(2).split(" ");
                String[] rotors2 = new String[numRotors];
                System.arraycopy(setting2, 0, rotors2, 0, numRotors);
                machine.insertRotors(rotors2);
                _setting = setting2[numRotors];
                setUp(machine, _setting);
                if (msg.contains("(")) {
                    int begin2 = msg.indexOf("(");
                    String pCycle2 = msg.substring(begin2);
                    Permutation plugPerm2 = new Permutation(pCycle2, _alphabet);
                    machine.setPlugboard(plugPerm2);
                } else {
                    String pCycle2 = "";
                    Permutation plugPerm2 = new Permutation(pCycle2, _alphabet);
                    machine.setPlugboard(plugPerm2);
                }
            } else {
                String outputStr = machine.convert(msg);
                printMessageLine(outputStr);
            }
        }
    }

    /** Check the rotor configuration.
     * @param check the string */
    private void checkRotors(String check) {
        if (check.contains("(")) {
            int split = check.indexOf("(");
            String[] preCheck = check.substring(2, split).split(" ");
            if (preCheck.length < numRotors + 1) {
                throw error("Wrong number of rotors");
            }
        } else {
            String[] preCheck = check.substring(2).split(" ");
            if (preCheck.length < numRotors + 1) {
                throw error("Wrong number of rotors");
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String alphabet = _config.nextLine();
            _alphabet = new Alphabet(alphabet);
            numRotors = _config.nextInt();
            numPawls = _config.nextInt();
            _config.nextLine();
            allRotors = new ArrayList<>();
            while (_config.hasNextLine()) {
                String rotorString = _config.nextLine();
                if (!rotorString.isBlank()) {
                    allRotors.add(readRotor(rotorString));
                }
            }
            return new Machine(_alphabet, numRotors, numPawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config.
     * @param str the string from which we construct a rotor. */
    private Rotor readRotor(String str) {
        try {
            if (_config.hasNext("\\(.*")) {
                String splitLine = _config.nextLine();
                int op = splitLine.indexOf("(");
                str = str + " " + splitLine.substring(op);
            }
            int split = str.indexOf("(");
            String[] condition = str.substring(0, split).trim().split(" ");
            String cycles = str.substring(split);
            String name = condition[0];
            String type = condition[1];
            Permutation p = new Permutation(cycles, _alphabet);
            if (type.charAt(0) == 'M') {
                String notches = type.substring(1);
                return new MovingRotor(name, p, notches);
            } else if (type.charAt(0) == 'N') {
                return new FixedRotor(name, p);
            } else {
                return new Reflector(name, p);
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        if (settings.length() != numRotors - 1) {
            throw error("Wrong format of settings");
        }
        M.setRotors(settings);
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        String retMsg = "";
        while (msg.length() > 5) {
            retMsg = retMsg + msg.substring(0, 5) + " ";
            msg = msg.substring(5);
        }
        retMsg += msg;
        retMsg = retMsg.trim();
        _output.println(retMsg);
    }


    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** Number of rotors in the machine. */
    private int numRotors;

    /** Number of pawls in the machine. */
    private int numPawls;

    /** The setting. */
    private String _setting;

    /** List of all rotors available for the machine. */
    private ArrayList<Rotor>  allRotors;

    /** File for encoded/decoded messages. */
    private PrintStream _output;
}
