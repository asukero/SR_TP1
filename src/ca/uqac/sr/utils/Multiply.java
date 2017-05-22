package ca.uqac.sr.utils;

/**
 * Created by Thoma on 22/05/2017.
 */
public class Multiply extends DoSomething {

    public Multiply(int number1, int number2) {
        super(number1, number2);
    }

    @Override
    public void compute() {
        result = number1 * number2;
    }

}
