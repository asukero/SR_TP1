package ca.uqac.sr; /**
 * Created by lowgr on 5/21/2017.
 */

import java.io.Serializable;

public class Message implements Serializable {
    public DoSomething doSomething = null;
    public int result = 0;

    private int number1;
    private int number2;

    public Message(DoSomething doSomething, int number1, int number2) {
        this.doSomething = doSomething;
        this.number1 = number1;
        this.number2 = number2;
    }

    public void computeResult() {
        this.result = this.doSomething.add(1, 2);
    }
}
