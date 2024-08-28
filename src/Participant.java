public class Participant {
    String name;
    int age, points;

    /**
     * Constructor to a participant
     *
     * @param name of Java_Individual.src.Participant
     * @param age  of Java_Individual.src.Participant
     */
    public Participant(String name, int age) {
        this.name = name;
        this.age = age;
        this.points = 0;
    }

    /**
     * @return name of Java_Individual.src.Participant of the calling instance
     */
    public String getName() {
        return name;
    }

    /**
     * @return age of Java_Individual.src.Participant of the calling instance
     */
    public int getAge() {
        return age;
    }

    /**
     * @return String description of Java_Individual.src.Participant of the calling instance
     * @Override
     */
    public String toString() {
        return "Participant [Name = " + name + ", Age = " + age + ", Points = " + points + "]";
    }

    /**
     * @return points earned by the Java_Individual.src.Participant of the calling instance
     */
    public int getPoints() {
        return points;
    }

    /**
     * @param points updated points
     */
    public void setPoints(int points) {
        this.points = points;
    }
}

