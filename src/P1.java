import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by owen on 4/9/17.
 */
public class P1 {

    public static final Object QLOCK = new Object();
    public static int _first = -1;
    public static Tuple _tuple = new Tuple();

    public static void main(String[] args){
        Server.set_name(args[0]);
        Scanner in = new Scanner(System.in);
        String userChoice = "";
        UIFSM fsm  = UIFSM.IDLE;

        String msg = "Client Start process ... \n";
        System.out.print(msg);

        // check folder path/file
        FileController.setFolder("/tmp/ylin", "777");
        FileController.setFolder("/tmp/ylin/linda", "777");
        FileController.setFolder("/tmp/ylin/linda/" + Server.get_name(), "777");
        FileController.setFile("/tmp/ylin/linda/" + Server.get_name() + "/nets.dat", "666");
        FileController.setFile("/tmp/ylin/linda/" + Server.get_name() + "/tuples.dat", "666");

        // --- Start server
        Thread thread = new Thread(new Server());
        thread.start();

        try{
            Thread.sleep(100);
        }catch (java.lang.InterruptedException e){
            System.out.print("InterruptedException\n");
        }

        // UI logic
        while(fsm != UIFSM.EXIT){
            switch (fsm){
                case IDLE:
                    msg = "linda>";
                    System.out.print(msg);
                    userChoice = in.nextLine();
                    String cmd = UIParser.cmd(userChoice);
                    if(cmd.equals("add")){
                        fsm = UIFSM.ADD;
                    }else if (cmd.equals("delete")){
                        fsm = UIFSM.DELETE;
                    }else if (cmd.equals("out")){
                        fsm = UIFSM.OUT;
                    }else if(cmd.equals("rd")){
                        fsm = UIFSM.RD;
                    }else if(cmd.equals("in")){
                        fsm = UIFSM.IN;
                    }else if(userChoice.toString().equals(":n")){
                        fsm = UIFSM.PRINTNETS;
                    }else if(userChoice.toString().equals(":t")){
                        fsm = UIFSM.PRINTTUPLES;
                    }
                    break;
                case ADD:
                    List<ServerInfo> serverInfos = UIParser.addMultipleParser(userChoice.toString());
                    for(ServerInfo s : serverInfos){
                        addRequest(s._ipAddr, s._port, Server._threadSafeList);
                    }
                    fsm = UIFSM.IDLE;
                    break;
                case DELETE:
                    fsm = UIFSM.IDLE;
                    break;
                case OUT:
                    Tuple tuple = new Tuple();
                    if(UIParser.outParser(userChoice.toString(), tuple) && tuple.get_qLocations().size() == 0){
                        int index = Md5sum(tuple, Server._threadSafeList.size());
                        outRequest(Server._threadSafeList.get(index)._ipAddr,
                                    Server._threadSafeList.get(index)._port,
                                    tuple);
                    }else{
                        System.out.print("Please enter the correct format.\n");
                    }
                    fsm = UIFSM.IDLE;
                    break;
                case RD:
                    tuple = new Tuple();
                    if(UIParser.outParser(userChoice.toString(), tuple)){
                        if(tuple.get_qLocations().size() == 0){
                            int index = Md5sum(tuple, Server._threadSafeList.size());
                            if(readRequest(Server._threadSafeList.get(index)._ipAddr,
                                    Server._threadSafeList.get(index)._port, tuple)){
                                msg = "rd tuple " + tuple.get_str() + " on " + Server._threadSafeList.get(index)._ipAddr+"\n";
                                System.out.print(msg);
                            }
                        }else{
                            // broadcast
                            ArrayList<Thread> threads = new ArrayList<>();
                            for(int i = 0; i<Server._threadSafeList.size();i++){
                                ServerInfo serverInfo = Server._threadSafeList.get(i);
                                threads.add(new Thread(new UIWorker(i, serverInfo._ipAddr, serverInfo._port, tuple, UIFSM.RD.getValue())));
                                threads.get(i).start();
                            }
                            synchronized (QLOCK){
                                try {
                                    QLOCK.wait();
                                    for(Thread th : threads){
                                        th.interrupt();
                                    }
                                    msg = "rd tuple " + _tuple.get_str() + " on " + Server._threadSafeList.get(P1._first)._ipAddr+"\n";
                                    System.out.print(msg);
                                    P1._first = -1;
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    fsm = UIFSM.IDLE;
                    break;
                case IN:
                    tuple = new Tuple();
                    if(UIParser.outParser(userChoice.toString(), tuple)){
                        int index = Md5sum(tuple, Server._threadSafeList.size());
                        System.out.print("Waiting for data... \n" );
                        if(inRequest(Server._threadSafeList.get(index)._ipAddr,
                                Server._threadSafeList.get(index)._port,
                                tuple)){
                            msg = "in tuple " + tuple.get_str() + " on " + Server._threadSafeList.get(index)._ipAddr+"\n";
                            System.out.print(msg);
                        }else{
                            msg = "Tuple does not exist.\n";
                            System.out.print(msg);
                        }
                    }
                    fsm = UIFSM.IDLE;
                    break;
                case PRINTNETS:
                    FileController.printNets(Server._threadSafeList);
                    fsm = UIFSM.IDLE;
                    break;
                case PRINTTUPLES:
                    printTuples();
                    fsm = UIFSM.IDLE;
                    break;
            }
        }
    }

    public static void addRequest(String ip, int port, CopyOnWriteArrayList<ServerInfo> list){
        try{
            Socket socket = new Socket(ip, port);
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
            os.writeInt(RequestProtocol.ADD);
            os.flush();
            os.writeObject(list);
            os.flush();
            int result = is.readInt();
            if(result != RequestProtocol.ACK){
                System.out.print("ACK ERROR\n");
            }
            socket.close();
        }catch (IOException e){
            System.out.print("IOException: " + e + "\n" );
        }
    }

    public static void outRequest(String ip, int port, Tuple tuple){
        try{
            Socket socket = new Socket(ip, port);
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
            os.writeInt(RequestProtocol.OUT);
            os.writeObject(tuple);
            os.flush();
            int result = is.readInt();
            if(result != RequestProtocol.ACK){
                System.out.print("ACK ERROR\n");
            }
            socket.close();
        }catch (IOException e){
            System.out.print("IOException P1: " + e + "\n" );
        }
    }

    public static boolean readRequest(String ip, int port, Tuple tuple){
        try{
            Socket socket = new Socket(ip, port);
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
            os.writeInt(RequestProtocol.RD);
            os.writeObject(tuple);
            os.flush();
            int result = is.readInt();
            if(_tuple.get_list() == null){
                _tuple.set_list(((Tuple) is.readObject()).get_list());
                os.writeInt(RequestProtocol.ACK);
                os.flush();
            }else{
                is.readObject();
                os.writeInt(RequestProtocol.ACK);
                os.flush();
            }
            is.close();
            os.close();
            socket.close();
            return result == RequestProtocol.HASTUPLE;
        }catch (IOException e){
            System.out.print("IOException: " + e + "\n" );
            return false;
        }catch (ClassNotFoundException e){
            System.out.print("ClassNotFoundException: " + e + "\n");
            return false;
        }
    }

    public static boolean inRequest(String ip, int port, Tuple tuple){
        try{
            Socket socket = new Socket(ip, port);
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
            os.writeInt(RequestProtocol.IN);
            os.writeObject(tuple);
            os.flush();
            int result = is.readInt();
            if(_tuple.get_list() == null){ // TODO: here need to be improved
                _tuple.set_list(((Tuple) is.readObject()).get_list());
                tuple.set_list(_tuple.get_list());
                os.writeInt(RequestProtocol.ACK);
                os.flush();
            }else{
                is.readObject();
                os.writeInt(RequestProtocol.ACK);
                os.flush();
            }
            is.close();
            os.close();
            socket.close();
            return result == RequestProtocol.HASTUPLE;
        }catch (IOException e){
            System.out.print("IOException: " + e + "\n" );
            return false;
        }catch (ClassNotFoundException e){
            System.out.print("ClassNotFoundException: " + e + "\n");
            return false;
        }
    }

    public static int Md5sum(Tuple tuple, int n){
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(tuple.get_str().getBytes());
            int result = ByteBuffer.wrap(array).getInt() % n;
            return result < 0 ? (-1)*result : result;
        }catch (NoSuchAlgorithmException e){
            System.out.print("NoSuchAlgorithmException");
            return -1;
        }
    }

    public static void printTuples(){
        for(Map.Entry<Tuple, Integer> entry : Server._concurrentHashMap.entrySet()){
            String msg = entry.getKey().get_str() + " : " + entry.getValue() + "\n";
            System.out.print(msg);
        }
    }
}
