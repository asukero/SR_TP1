package ca.uqac.sr; /**
 * Created by lowgr on 5/21/2017.
 */

import java.io.Serializable;

public class DoSomething implements Serializable {
    public DoSomething() {

    }

    public int add(int number1, int number2){
        return number1 + number2;
    }
}
