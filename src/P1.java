import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                    }else if(cmd.equals("in")){
                        fsm = UIFSM.IN;
                    }else if(userChoice.toString().equals(":p")){
                        fsm = UIFSM.PRINTNETS;
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
                    List<Element> list = new ArrayList<>();
                    if(UIParser.outParser(userChoice.toString(), list)){
                        //outRequest(ip.toString(), Integer.parseInt(port.toString()), Server._threadSafeList);
                    }else{
                        System.out.print("Please enter the correct format.\n");
                    }
                    fsm = UIFSM.IDLE;
                    break;
                case IN:
                    fsm = UIFSM.IDLE;
                    break;
                case PRINTNETS:
                    FileController.printNets(Server._threadSafeList);
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

    public static void outRequest(String ip, int port, List<Element> list){
        try{
            Socket socket = new Socket(ip, port);
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            os.writeInt(RequestProtocol.OUT);
            os.writeObject(list);
            os.close();
            socket.close();
        }catch (IOException e){
            System.out.print("IOException: " + e + "\n" );
        }
    }

}
