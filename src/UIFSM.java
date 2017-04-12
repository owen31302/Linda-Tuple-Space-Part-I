/**
 * Created by owen on 4/9/17.
 */
public enum UIFSM {
    IDLE(0), ADD(1), DELETE(2), OUT(3), IN(4), EXIT(5), PRINTNETS(6), RD(7), PRINTTUPLES(8);

    private int _value;
    public static int count = UIFSM.values().length;

    UIFSM(int value){
        this._value = value;
    }

    public int getValue(){
        return _value;
    }
}
