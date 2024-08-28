import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Scanner;


public class QuizApp extends Thread {
    static Scanner sc = new Scanner(System.in);
    // List of participants
    public static LinkedList<Participant> participants = new LinkedList<>();

    // List of randomly selected questions
    protected static ArrayList<Integer> randomQues = new ArrayList<>(10);

    // list of right answers of asked questions
    protected static ArrayList<String> answers = new ArrayList<>(10);

    // list of answers given by participant
    protected static ArrayList<String> givenAnswers = new ArrayList<>(10);
    private static CallableStatement cst = null;
    private static Connection con = null;
    static String category = null;
    static int random = 0;
    static String que = "", a = "", b = "", c = "", d = "";
    static boolean quizTie = false;

    /**
     * @Override To ask questions from selected category to a particular Thread(Participant)
     * and to calculate the points earned by that Participant
     */
    public void run() {
        Participant selectedParticipant = null;
        synchronized (this) {
            for (Participant participant : participants) {
                if (Thread.currentThread().getName().equals(participant.getName())) {
                    selectedParticipant = participant;
                    break;
                }
            }

            assert selectedParticipant != null;
            System.out.println("\n-> " + selectedParticipant.getName() + "'s turn to attempt Quiz -------------------------");
            try {
                int size = quizTie ? 5 : 10;
                for (int i = 1; i <= size; i++) {
                    switch (category) {
                        case "gk" -> getGKQue(i);
                        case "history" -> getHistoryQue(i);
                        case "geography" -> getGeographyQue(i);
                        case "sci" -> getScienceQue(i);
                        case "maths" -> getMathsQue(i);
                        default -> throw new Exception("Invalid Category Selected");
                    }
                }
            } catch (Exception e) {
                System.out.println("ERROR : " + e.getMessage());
            }

            calculateResult(selectedParticipant);
        }
    }

    /**
     * To run the Quiz Application Program
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/quiz_application", "root", "");
        if (con != null) {
            System.out.println("Connection Established to the DataBase");
        } else {
            System.out.println("Connection Failed!!");
        }
        System.out.println("\n---------------- WELCOME TO QUIZ APPLICATION ----------------\n");
        while (true) {
            System.out.println("""
                    \n--------- QUIZ MENU ---------
                    1 : Add Participants
                    2 : Display Participants
                    3 : Remove Participant
                    4 : Attempt Quiz & Display Results
                    0 : Exit""");
            try {
                System.out.print("Enter Choice: ");
                int choice = Integer.parseInt(sc.nextLine().trim());
                switch (choice) {
                    case 1:
                        System.out.print("Enter Count of Participants To Attempt QUIZ: ");
                        int size = sc.nextInt();
                        sc.nextLine(); // Consume New Line
                        addParticipants(size);
                        break;

                    case 2:
                        displayParticipants();
                        break;

                    case 3:
                        System.out.print("Enter Participant Name To Remove : ");
                        String name = sc.nextLine();
                        if (participants.isEmpty()) {
                            System.out.println("No Participants in the List");
                        } else if (removeParticipant(name)) {
                            System.out.println("Participant " + name + " Removed Successfully");
                        } else {
                            System.out.println("Participant " + name + " Not Found.");
                        }
                        break;

                    case 4:
                        if (participants.size() < 2) {
                            System.out.println("Minimum 2 Participants are required to attempt QUIZ.");
                        } else {
                            attemptQuiz();
                            sc.nextLine(); // Consume New Line
                            displayResults();
                        }
                        break;

                    case 0:
                        con.close();
                        sc.close();
                        System.out.println("------------- EXITING QUIZ APPLICATION -------------");
                        System.exit(0);

                    default:
                        System.out.println("Invalid Input, Try Again !!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid Input, Try Again !!");
            } catch (Exception e) {
                System.out.println("ERROR : " + e.getMessage());
                sc.nextLine(); // Consume New Line
            }
        }
    }

    /**
     * adds participants in the list
     * @param size of Participant List
     */
    private static void addParticipants(int size) {
        for (int i = 1; i <= size; i++) {
            try {
                System.out.print("\nEnter Participant-" + i + " Name : ");
                String name = sc.nextLine();
                System.out.print("Enter Participant-" + i + " Age  : ");
                int age = sc.nextInt();
                sc.nextLine(); // Consume New Line
                participants.add(new Participant(name, age));
                participants.sort(Comparator.comparing(Participant::getAge));
                System.out.println(name + " added in participants list");
            } catch (Exception e) {
                System.out.println("Invalid Input !!!");
                sc.nextLine(); // Consume New Line
            }
        }
    }

    /**
     * @param name of Participant
     * @return boolean value that participant is removed or not
     */
    private static boolean removeParticipant(String name) {
        for (Participant participant : participants) {
            if (name.equalsIgnoreCase(participant.getName())) {
                return participants.remove(participant);
            }
        }
        return false;
    }

    /**
     * displays List of Participants
     */
    private static void displayParticipants() {
        if (participants.isEmpty()) {
            System.out.println("No Participants in the List");
            return;
        }

        System.out.println("------------------ LIST OF PARTICIPANTS ------------------");
        for (Participant participant : participants) {
            System.out.println(participant.toString());
        }
        System.out.println("----------------------------------------------------------");
    }

    /**
     * To run all Participant Threads by attempting quiz
     */
    private static void attemptQuiz() {
        while (category == null) {
            category = selectCategory();
        }

        for (Participant participant : participants) {
            QuizApp q = new QuizApp();
            q.setName(participant.getName());
            q.start();
            try {
                q.join();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
        category = null;
    }

    /**
     * @return Quiz Category selected by the user
     */
    public static String selectCategory() {
        System.out.println("""
                \n------------- QUIZ CATEGORIES -------------
                1 : General Knowledge
                2 : History
                3 : Geography
                4 : Science
                5 : Maths
                """);
        System.out.print("Enter Your Choice : ");
        int ch = Integer.parseInt(sc.nextLine().trim());
        return switch (ch) {
            case 1 -> "gk";
            case 2 -> "history";
            case 3 -> "geography";
            case 4 -> "sci";
            case 5 -> "maths";
            default -> {
                System.out.println("Invalid Category Selected, Try Again!!");
                yield null;
            }
        };
    }

    /**
     * @return randomly selected Question number
     */
    public static int getRandomQue() {
        boolean inValid = true;
        while (inValid || random == 0) {
            random = (int) (Math.random() * 30);
            inValid = randomQues.contains(random);
        }
        randomQues.add(random);
        return random;
    }

    /**
     * to get GK Question from the DataBase using instance of CallableStatement
     *
     * @param num of Question
     */
    public synchronized static void getGKQue(int num) throws SQLException {
        cst = con.prepareCall("{call getGKQue(?,?,?,?,?,?,?)}");

        cst.setInt(1, getRandomQue());
        cst.execute();
        answers.add(cst.getString("answer"));
        que = cst.getString(2);
        a = cst.getString("opA");
        b = cst.getString("opB");
        c = cst.getString("opC");
        d = cst.getString("opD");
        askQuestion(num, que, a, b, c, d);
    }

    /**
     * to get History Question from the DataBase using instance of CallableStatement
     *
     * @param num of Question
     */
    public synchronized static void getHistoryQue(int num) throws SQLException {
        cst = con.prepareCall("{call getHistoryQue(?,?,?,?,?,?,?)}");

        cst.setInt(1, getRandomQue());
        cst.execute();
        answers.add(cst.getString("answer"));
        que = cst.getString(2);
        a = cst.getString("opA");
        b = cst.getString("opB");
        c = cst.getString("opC");
        d = cst.getString("opD");
        askQuestion(num, que, a, b, c, d);
    }


    /**
     * to get Geography Question from the DataBase using instance of CallableStatement
     *
     * @param num of Question
     */
    public synchronized static void getGeographyQue(int num) throws SQLException {
        cst = con.prepareCall("{call getGeographyQue(?,?,?,?,?,?,?)}");

        cst.setInt(1, getRandomQue());
        cst.execute();
        answers.add(cst.getString("answer"));
        que = cst.getString(2);
        a = cst.getString("opA");
        b = cst.getString("opB");
        c = cst.getString("opC");
        d = cst.getString("opD");
        askQuestion(num, que, a, b, c, d);
    }


    /**
     * to get Science Question from the DataBase using instance of CallableStatement
     *
     * @param num of Question
     */
    public synchronized static void getScienceQue(int num) throws SQLException {
        cst = con.prepareCall("{call getScienceQue(?,?,?,?,?,?,?)}");

        cst.setInt(1, getRandomQue());
        cst.execute();
        answers.add(cst.getString("answer"));
        que = cst.getString(2);
        a = cst.getString("opA");
        b = cst.getString("opB");
        c = cst.getString("opC");
        d = cst.getString("opD");
        askQuestion(num, que, a, b, c, d);
    }


    /**
     * to get maths Question from the DataBase using instance of CallableStatement
     *
     * @param num of Question
     */
    public synchronized static void getMathsQue(int num) throws SQLException {
        cst = con.prepareCall("call getMathsQue(?,?,?,?,?,?,?)");

        cst.setInt(1, getRandomQue());
        cst.execute();
        answers.add(cst.getString("answer"));
        que = cst.getString(2);
        a = cst.getString("opA");
        b = cst.getString("opB");
        c = cst.getString("opC");
        d = cst.getString("opD");
        askQuestion(num, que, a, b, c, d);
    }

    /**
     * the question is asked to the particular Participant
     *
     * @param num         of question
     * @param que,a,b,c,d question and its options
     */
    private synchronized static void askQuestion(int num, String que, String a, String b, String c, String d) {
        System.out.println("\nQue-" + num + ": " + que);
        System.out.printf("(A) %-30s\t(B) %-30s\n", a, b);
        System.out.printf("(C) %-30s\t(D) %-30s\n", c, d);
        System.out.print("Answer Option: ");
        String ans = sc.next();
        while (!(ans.equalsIgnoreCase("a") || ans.equalsIgnoreCase("b") ||
                ans.equalsIgnoreCase("c") || ans.equalsIgnoreCase("d"))) {
            System.out.print("Answer Valid Option: ");
            ans = sc.next();
        }
        givenAnswers.add(ans);
    }

    /**
     * To calculate points earned by particular Participant
     *
     * @param participant instance of particular Participant
     */
    public synchronized static void calculateResult(Participant participant) {
        int num = quizTie ? 5 : 10;
        for (int i = 0; i < num; i++) {
            System.out.println(randomQues.get(i) + " = " + answers.get(i) + " & " + Thread.currentThread().getName() + " gave " + givenAnswers.get(i));
            if (answers.get(i).equalsIgnoreCase(givenAnswers.get(i))) {
                participant.points++;
            }
        }
        answers.clear();
        givenAnswers.clear();
        randomQues.clear();
    }

    /**
     * Displays the results of all participants
     */
    public static void displayResults() {
        participants.sort(Comparator.comparing(Participant::getPoints).reversed());
        Participant first = participants.get(0);
        Participant second = participants.get(1);
        if (first.getPoints() == second.getPoints()) {
            first.setPoints(0);
            second.setPoints(0);
            quizTie = true;
            System.out.println("We have a tie between " + first.getName() + " and " + second.getName());
            System.out.println("Attempt the TIE BREAKER QUIZ to break the tie !!!");
            tieBreaker();
            sc.nextLine(); // Consume New Line
        }
        if (!quizTie) {
            System.out.println("\n------------------------ QUIZ RESULTS ------------------------");
            for (Participant participant : participants) {
                double percent = participant.getPoints() * 10;
                System.out.println(participant + " with " + percent + " %");
                participant.setPoints(0);
            }
        }
        System.out.println("\nWinner Participant : " + participants.getFirst());
    }

    /**
     * To attempt the quiz again to break the tie.
     */
    private static void tieBreaker() {
        for (int i = 0; i < 2; i++) {
            while (category == null) {
                category = selectCategory();
            }
            QuizApp q = new QuizApp();
            q.setName(participants.get(i).getName());
            q.start();
            try {
                q.join();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
