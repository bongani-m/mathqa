package dekauliya.fyp.mathqa.Models;

/**
 * Created by dekauliya on 29/1/17.
 */

public class Paper {
    private String id;
    private int year;
    private String month;
    private int number;
    private int noOfQuestion;

    public Paper(String id, int year, String month, int number, int noOfQuestion) {
        this.id = id;
        this.year = year;
        this.month = month;
        this.number = number;
        this.noOfQuestion = noOfQuestion;
    }

    public Paper(int year, String month, int number, int noOfQuestion) {
        this.year = year;
        this.month = month;
        this.number = number;
        this.noOfQuestion = noOfQuestion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getNoOfQuestion() {
        return noOfQuestion;
    }

    public void setNoOfQuestion(int noOfQuestion) {
        this.noOfQuestion = noOfQuestion;
    }
}