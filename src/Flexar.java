/**
 * Created by - Soham Kulkarni
 * www.github.com/Zoham
 *
 * infix regex to NFA
 * NFA string acceptance
 */

import java.util.*;

public class Flexar {
    static Map<Integer, State> transTable = null;
    static Map<Integer, List<Integer>> levelMapper = null;

    static int first = -1;
    static int last = -1;

    public static void main(String[] args) {
        transTable = new TreeMap();
        levelMapper = new TreeMap<>();

        Scanner sc = new Scanner(System.in);
        String input = sc.next().trim();

        //String input = "()";

        Map<Integer, List<List<String>>> partsByLevel = getPartsByLevel(input);
        Map<Integer, List<List<State>>> graphyByLevel =
                generatePartsByLevel(partsByLevel);

        System.out.println(input);


        for (int level : graphyByLevel.keySet()) {
            List<List<State>> lolos = graphyByLevel.get(level);
            for (List<State> los : lolos) {
                for (State state : los) {
                    transTable.put(state.state, state);
                }
            }
        }

        System.out.println("St\tx\ty\tz\te");
        for (int key : transTable.keySet()) {
            State state = transTable.get(key);
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

        while (true) {
            System.out.print("Enter String: ");
            String str = sc.next();
            System.out.println(check(str));
        }
    }

    // FUNCTIONS ----------------------------------------------------------

    /**
     * CHECK IF STRING IS ACCEPTED
     *
     * @param str
     * @return
     */
    static boolean check(String str) {
        List<Integer> possible = new ArrayList<>();
        possible.add(first);
        recurseFill(possible, 'e');

        for (int i = 0; i < str.length(); i++) {
            recurseFill(possible, str.charAt(i));
            recurseFill(possible, 'e');
            if(possible.contains(last)){
                return true;
            }
        }

        return false;
    }

    /**
     * GENERATE GRAPH BASED ON BRACKET LEVEL
     *
     * @param partsByLevel
     * @return
     */
    static Map<Integer, List<List<State>>> generatePartsByLevel(
            Map<Integer, List<List<String>>> partsByLevel) {
        Map<Integer, List<List<List<State>>>> graphByOR = new TreeMap<>();
        Map<Integer, List<List<State>>> graphByLevel = new TreeMap<>();

        for (int level = Collections.max(partsByLevel.keySet()); level >= 0; level--) {
            List<List<List<State>>> levelParts = new ArrayList<>();
            List<Integer> levelWildCards = levelMapper.get(level + 1);
            int wildCardIndex = 0;

            for (List<String> partByLevel : partsByLevel.get(level)) {
                List<List<State>> orParts = new ArrayList<>();
                for (String orPart : partByLevel) {
                    List<State> eachOrPart = new ArrayList<>();
                    State start = new State(Type.START);

                    State current = start;
                    eachOrPart.add(start);

                    int wildCards = -1;
                    if (orPart.endsWith("*")) {
                        wildCards = levelWildCards.get(wildCardIndex);
                        for (int i = 0; i < orPart.length(); i++) {
                            if (wildCards != -1) {
                                wildCards--;
                                if (wildCards == 0) {
                                    start.e.add(graphByLevel.get(level + 1).get(wildCardIndex).get(0));
                                    current = graphByLevel.get(level + 1).get(wildCardIndex)
                                            .get(graphByLevel.get(level + 1).get(wildCardIndex).size() - 1);

                                    State fin = new State(Type.FIN);
                                    current.e.add(fin);
                                    fin.e.add(start);
                                    eachOrPart.add(fin);

                                    wildCardIndex++;
                                }
                            }
                        }
                    } else {
                        for (int i = 0; i < orPart.length(); i++) {
                            State newState = null;
                            switch (orPart.charAt(i)) {
                                case '.':
                                    break;
                                case 'x':
                                    newState = new State(Type.NONE);
                                    current.x.add(newState);
                                    current = newState;
                                    eachOrPart.add(newState);
                                    break;
                                case 'y':
                                    newState = new State(Type.NONE);
                                    current.y.add(newState);
                                    current = newState;
                                    eachOrPart.add(newState);
                                    break;
                                case 'z':
                                    newState = new State(Type.NONE);
                                    current.z.add(newState);
                                    current = newState;
                                    eachOrPart.add(newState);
                                    break;
                                case '#':
                                    wildCards = levelWildCards.get(wildCardIndex);
                                    while (i < orPart.length() && orPart.charAt(i) == '#') {
                                        if (wildCards != -1) {
                                            wildCards--;
                                            if (wildCards == 0) {
                                                current.e.add(graphByLevel.get(level + 1).get(wildCardIndex).get(0));
                                                current = graphByLevel.get(level + 1).get(wildCardIndex)
                                                        .get(graphByLevel.get(level + 1).get(wildCardIndex).size() - 1);

                                                newState = new State(Type.NONE);
                                                current.e.add(newState);
                                                current = newState;
                                                eachOrPart.add(newState);

                                                wildCardIndex++;
                                            }
                                        }
                                        i++;
                                    }
                                    i--;
                                    break;
                                default:
                                    System.out.println("Invalid Regex");
                            }
                        }

                        if (current.state == start.state) {
                            State fin = new State(Type.FIN);
                            current.e.add(fin);
                            eachOrPart.add(fin);
                        } else {
                            current.type = Type.FIN;
                        }
                    }

                    orParts.add(eachOrPart);
                }
                levelParts.add(orParts);
            }
            graphByOR.put(level, levelParts);

            List<List<State>> levelParts2 = new ArrayList<>();
            for (List<List<State>> partByLevel : graphByOR.get(level)) {
                List<State> orParts = new ArrayList<>();
                State start = new State(Type.START);
                State fin = new State(Type.FIN);

                first = start.state;
                last = fin.state;
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
                levelParts2.add(orParts);
            }
            graphByLevel.put(level, levelParts2);
        }

        for (
                int level = Collections.max(graphByOR.keySet());
                level >= 0; level--) {
            List<List<List<State>>> lololos = graphByOR.get(level);
            for (List<List<State>> lolos : lololos) {
                for (List<State> los : lolos) {
                    for (State state : los) {
                        transTable.put(state.state, state);
                    }
                }
            }
        }

        return graphByLevel;
    }

    /**
     * SPLIT STRING BY BRACKET LEVEL
     * @param str
     * @return
     */
    static Map<Integer, List<List<String>>> getPartsByLevel(String str) {
        Map<Integer, List<String>> partsByLevel = new TreeMap<>();
        Map<Integer, Map<Integer, Integer>> brackets = getMatchingBrackets(str);
        String newString = "";

        for (int level = Collections.max(brackets.keySet()); level >= 1; level--) {
            List<String> parts = new ArrayList<>();
            List<Integer> levelMap = new ArrayList<>();

            for (int open : brackets.get(level).keySet()) {
                parts.add(str.substring(open + 1, brackets.get(level).get(open)));

                newString = str.substring(0, open);
                for (int i = open; i <= brackets.get(level).get(open); i++) {
                    newString += '#';
                }
                str = newString + str.substring(brackets.get(level).get(open) + 1);

                levelMap.add(brackets.get(level).get(open) - open + 1);
            }
            partsByLevel.put(level, parts);
            levelMapper.put(level, levelMap);
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

    static void recurseFill(List<Integer> possible, char list) {
        for (int i = 0; i<possible.size();i++) {
            State state = transTable.get(possible.get(i));
            switch (list) {
                case 'e':
                    if (!state.e.isEmpty()) {
                        possible.remove(Integer.valueOf(state.state));
                    }

                    for (State st : state.e) {
                        if(!possible.contains(st.state)) {
                            possible.add(st.state);
                            recurseFill(possible, list);
                        }
                    }
                    break;
                case 'x':
                    if (!state.x.isEmpty()) {
                        possible.remove(Integer.valueOf(state.state));
                    }

                    for (State st : state.x) {
                        if(!possible.contains(st.state)) {
                            possible.add(st.state);
                            recurseFill(possible, list);
                        }
                    }
                    break;
                case 'y':
                    if (!state.y.isEmpty()) {
                        possible.remove(Integer.valueOf(state.state));
                    }

                    for (State st : state.y) {
                        if(!possible.contains(st.state)) {
                            possible.add(st.state);
                            recurseFill(possible, list);
                        }
                    }
                    break;
                case 'z':
                    if (!state.z.isEmpty()) {
                        possible.remove(Integer.valueOf(state.state));
                    }

                    for (State st : state.z) {
                        if(!possible.contains(st.state)) {
                            possible.add(st.state);
                            recurseFill(possible, list);
                        }
                    }
                    break;
            }
        }
    }
}

// CUSTOM DATA STRUCTURES ------------------------------------------------

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
