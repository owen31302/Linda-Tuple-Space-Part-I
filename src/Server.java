import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by owen on 4/7/17.
 */
public class Server implements Runnable{
    static private int _id = 0;
    static private String _name;
    static private String _ip;
    static private int _port;
    static public CopyOnWriteArrayList<ServerInfo> _threadSafeList = new CopyOnWriteArrayList<>();
    static public ConcurrentHashMap<Tuple, Integer> _concurrentHashMap = new ConcurrentHashMap<>();

    public static final Lock LOCK = new Lock();

    @Override
    public void run() {

        String msg = "Server Start process ... \n";
        System.out.print(msg);

        try{

            ServerSocket s = new ServerSocket(0);
            _ip = InetAddress.getLocalHost().getHostAddress();
            _port = s.getLocalPort();
            System.out.println(_ip + " at port number: " + _port);

            // read host info from nets.dat file
            FileController.readNets("/tmp/ylin/linda/" + Server.get_name() + "/nets.dat", _threadSafeList);
            FileController.readTuples("/tmp/ylin/linda/" + Server.get_name() + "/tuples.dat", _concurrentHashMap);
            // check if current host name existed in the list before or newly created one
            if(!FileController.checkHost(_id, _name, _ip, _port, _threadSafeList)){
                System.out.print("Please enter the same host name.\n");
                Thread.currentThread().interrupt();
                return;
            }

            while(!s.isClosed()){
                Socket clientSocket = s.accept();
                //System.out.print("I got a client\n");
                Thread thread = new Thread(new Worker(clientSocket));
                thread.start();
            }
        }catch (IOException e){
            System.out.print("IOException: " + e + "\n" );
        }
    }

    public static String get_name(){
        return _name;
    }
    public static int get_port(){
        return _port;
    }
    public static String get_ip(){
        return _ip;
    }
    public static void set_name(String s){
        _name = s;
    }
    public static void set_port(int p){
        _port = p;
    }
    public static void set_ip(String ip){
        _ip = ip;
    }
}
