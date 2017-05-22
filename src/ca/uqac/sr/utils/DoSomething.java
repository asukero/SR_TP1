package ca.uqac.sr.utils; /**
 * Created by lowgr on 5/21/2017.
 */

import java.io.Serializable;

public abstract class DoSomething implements Serializable {

    int result = 0;
    int number1;
    int number2;

    public DoSomething(int number1, int number2) {
        this.number1 = number1;
        this.number2 = number2;
    }

    public abstract void compute();

    public int getResult() {
        return result;
    }
}
