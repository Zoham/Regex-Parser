import java.util.*;
import java.lang.System.*;

public class Flexar {
    static List<State> transTable = null;

    public static void main(String[] args) {
        transTable = new ArrayList<>();

//        Scanner sc = new Scanner(System.in);
//        String input = sc.next().trim();

        String input = "x.(x+((z.x)*+x.y.z)*+z).y.z+(z.y)";

        Map<Integer, List<List<String>>> partsByLevel = getPartsByLevel(input);
        Map<Integer, List<List<State>>> graphyByLevel =
                generatePartsByLevel(partsByLevel);

        System.out.println("Testing");
        System.out.println(input);

        System.out.println("St\tx\ty\tz\te");
        for (int level : graphyByLevel.keySet()) {
            List<List<State>> lolos = graphyByLevel.get(level);
            for (List<State> los : lolos) {
                transTable.addAll(los);
            }
        }

        for (State state : transTable) {
            System.out.print(state.state + "\t");

            if (state.x.isEmpty()) System.out.print("-\t");
            else {
                for (State hop : state.x) System.out.print(hop.state + ",");
                System.out.print("\t");
            }
            if (state.y.isEmpty()) System.out.print("-\t");
            else {
                for (State hop : state.y) System.out.print(hop.state + ",");
                System.out.print("\t");
            }
            if (state.z.isEmpty()) System.out.print("-\t");
            else {
                for (State hop : state.z) System.out.print(hop.state + ",");
                System.out.print("\t");
            }
            if (state.e.isEmpty()) System.out.print("-\t");
            else {
                for (State hop : state.e) System.out.print(hop.state + ",");
                System.out.print("\t");
            }
            System.out.println();
        }
    }

    // FUNCTIONS ----------------------------------------------------------

    static Map<Integer, List<List<State>>> generatePartsByLevel(
            Map<Integer, List<List<String>>> partsByLevel) {
        Map<Integer, List<List<List<State>>>> graphByOR = new TreeMap<>();
        for (int level : partsByLevel.keySet()) {
            List<List<List<State>>> levelParts = new ArrayList<>();
            for (List<String> partByLevel : partsByLevel.get(level)) {
                List<List<State>> orParts = new ArrayList<>();
                for (String orPart : partByLevel) {
                    List<State> eachOrPart = new ArrayList<>();
                    State start = new State(Type.START);
                    State fin = new State(Type.FIN);

                    State current = start;
                    eachOrPart.add(start);
                    // only concat
                    if (orPart.indexOf('#') >= 0) {
                    } else {
                        for (int i = 0; i < orPart.length(); i++) {
                            State newState = new State(Type.NONE);
                            switch (orPart.charAt(i)) {
                                case '.':
                                    break;
                                case 'x':
                                    current.x.add(newState);
                                    current = newState;
                                    eachOrPart.add(newState);
                                    break;
                                case 'y':
                                    current.y.add(newState);
                                    current = newState;
                                    eachOrPart.add(newState);
                                    break;
                                case 'z':
                                    current.z.add(newState);
                                    current = newState;
                                    eachOrPart.add(newState);
                                    break;
                                default:
                                    System.out.println("Invalid Regex");
                            }
                        }
                    }

                    current.e.add(fin);
                    eachOrPart.add(fin);

                    orParts.add(eachOrPart);
                }
                levelParts.add(orParts);
            }
            graphByOR.put(level, levelParts);
        }

        for (int level : graphByOR.keySet()) {
            List<List<List<State>>> lololos = graphByOR.get(level);
            for (List<List<State>> lolos : lololos) {
                for (List<State> los : lolos) {
                    if (!los.isEmpty())
                        transTable.addAll(los);
                }
            }
        }

        Map<Integer, List<List<State>>> graphByLevel = new TreeMap<>();
        for (int level : partsByLevel.keySet()) {
            List<List<State>> levelParts = new ArrayList<>();
            for (List<List<State>> partByLevel : graphByOR.get(level)) {
                List<State> orParts = new ArrayList<>();
                State start = new State(Type.START);
                State fin = new State(Type.FIN);
                for (List<State> orPart : partByLevel) {
                    for (State eachState : orPart) {
                        if (eachState.type == Type.START) {
                            start.e.add(eachState);
                            eachState.type = Type.NONE;
                        }
                        if (eachState.type == Type.FIN) {
                            eachState.e.add(fin);
                            eachState.type = Type.NONE;
                        }
                    }
                }
                if (start.isAlone()) {
                    start.e.add(fin);
                }
                orParts.add(start);
                orParts.add(fin);
                levelParts.add(orParts);
            }
            graphByLevel.put(level, levelParts);
        }

        return graphByLevel;
    }

    static Map<Integer, List<List<String>>> getPartsByLevel(String str) {
        Map<Integer, List<String>> partsByLevel = new TreeMap<>();
        Map<Integer, Map<Integer, Integer>> brackets = getMatchingBrackets(str);
        String newString = "";

        for (int level = Collections.max(brackets.keySet()); level >= 1; level--) {
            List<String> parts = new ArrayList<>();
            for (int open : brackets.get(level).keySet()) {
                parts.add(str.substring(open + 1, brackets.get(level).get(open)));

                newString = str.substring(0, open);
                for (int i = open; i <= brackets.get(level).get(open); i++) {
                    newString += '#';
                }
                str = newString + str.substring(brackets.get(level).get(open) + 1);
            }
            partsByLevel.put(level, parts);
        }

        List<String> parts = new ArrayList<>();
        parts.add(str);
        partsByLevel.put(0, parts);

        Map<Integer, List<List<String>>> partsByLevelOr = new TreeMap<>();
        for (int level = Collections.max(brackets.keySet()); level >= 0; level--) {
            List<List<String>> splitParts = new ArrayList<>();
            for (String partByLevel : partsByLevel.get(level)) {
                splitParts.add(Arrays.asList(partByLevel.split("[+]")));
            }
            partsByLevelOr.put(level, splitParts);
        }

        return partsByLevelOr;
    }


    // HELPER FUNCTIONS --------------------------------------------------

    static Map<Integer, Map<Integer, Integer>> getMatchingBrackets(String str) {
        Map<Integer, Map<Integer, Integer>> brackets = new TreeMap<>();
        List<Integer> open = new ArrayList<Integer>();

        int level = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '(') {
                open.add(0, Integer.valueOf(i));
                level++;
            } else if (str.charAt(i) == ')') {
                if (brackets.get(level) == null) {
                    brackets.put(level, new TreeMap());
                }
                brackets.get(level).put(open.get(0), Integer.valueOf(i));
                open.remove(0);
                level--;
            }
        }

        return brackets;
    }
}

// CUSTOM DATA STRUCTURE ------------------------------------------------

class State {
    static int count = 0;
    int state = 0;
    List<State> x = null;
    List<State> y = null;
    List<State> z = null;
    List<State> e = null;
    Type type;

    public State(Type type) {
        this.state = count++;
        this.x = new ArrayList<>();
        this.y = new ArrayList<>();
        this.z = new ArrayList<>();
        this.e = new ArrayList<>();
        this.type = type;
    }

    public boolean isAlone() {
        if (this.x.isEmpty() && this.y.isEmpty() &&
                this.z.isEmpty() && this.e.isEmpty()) return true;
        return false;
    }
}

enum Type {
    START, FIN, NONE;
}
