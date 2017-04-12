import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by owen on 4/9/17.
 */
public class P1 {
    public static void main(String[] args){
        Server.set_name(args[0]);
        Scanner in = new Scanner(System.in);
        String userChoice = "";
        UIFSM fsm  = UIFSM.IDLE;

        ObjectOutputStream oos;
        ObjectInputStream ois;

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
                        try{
                            Thread.sleep(250);
                        }catch (InterruptedException e){
                            System.out.print("InterruptedException\n");
                        }
                    }
                    fsm = UIFSM.IDLE;
                    break;
                case DELETE:
                    fsm = UIFSM.IDLE;
                    break;
                case OUT:
                    Tuple tuple = new Tuple();
                    if(UIParser.outParser(userChoice.toString(), tuple)){
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
                        int index = Md5sum(tuple, Server._threadSafeList.size());
                        if(readRequest(Server._threadSafeList.get(index)._ipAddr,
                                Server._threadSafeList.get(index)._port,
                                tuple)){
                            msg = "rd tuple " + tuple.get_str() + " on " + Server._threadSafeList.get(index)._ipAddr+"\n";
                            System.out.print(msg);
                        }else{
                            msg = "Tuple does not exist.\n";
                            System.out.print(msg);
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
            os.writeInt(RequestProtocol.ADD);
            os.writeObject(list);
            os.close();
            socket.close();
        }catch (IOException e){
            System.out.print("IOException: " + e + "\n" );
        }
    }

    public static void outRequest(String ip, int port, Tuple tuple){
        try{
            Socket socket = new Socket(ip, port);
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            os.writeInt(RequestProtocol.OUT);
            os.writeObject(tuple);
            os.flush();
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
            int result = is.readInt();
            is.close();
            os.close();
            socket.close();
            return result == RequestProtocol.HASTUPLE;
        }catch (IOException e){
            System.out.print("IOException: " + e + "\n" );
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
            if(result == RequestProtocol.NOTUPLE){
                System.out.print("There is no tuple.\n");
            }else{
                os.writeInt(RequestProtocol.ACK);
                os.flush();
            }
            is.close();
            os.close();
            socket.close();
            return true;
        }catch (IOException e){
            System.out.print("IOException: " + e + "\n" );
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
