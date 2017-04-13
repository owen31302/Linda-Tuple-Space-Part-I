/**
 * Created by owen on 4/12/17.
 */
public class UIWorker implements Runnable {
    private int _id;
    private String _ip;
    private int _port;
    private Tuple _tuple;
    private int _operation;

    UIWorker(int id, String ip, int port, Tuple tuple, int operation){
        _id = id;
        _ip = ip;
        _port = port;
        _tuple = tuple;
        _operation = operation;
    }

    @Override
    public void run() {
        if(_operation == UIFSM.RD.getValue()){
            if(P1.readRequest(_ip, _port, _tuple)){
                synchronized (P1.QLOCK) {
                    if(P1._first == -1){
                        P1._first = _id;
                    }
                    P1.QLOCK.notifyAll();
                }
            }
        }else if (_operation == UIFSM.IN.getValue()){

        }else{
            System.out.print("Wrong command.\n");
        }
    }
}
