import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by owen on 4/9/17.
 */
public class Worker implements Runnable {
    private Socket _clientSocket;

    Worker(Socket clientSocket){
        _clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try{
            ObjectOutputStream oos = new ObjectOutputStream(_clientSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(_clientSocket.getInputStream());
            int action = ois.readInt();
            switch (action){
                case RequestProtocol.ADD:
                    System.out.print("RequestProtocol: ADD\n");
                    // Receive the other one's nets file.
                    CopyOnWriteArrayList<ServerInfo> threadSafeList = (CopyOnWriteArrayList<ServerInfo>)ois.readObject();
                    ois.close();
                    _clientSocket.close();

                    // Traverse nets file
                    CopyOnWriteArrayList<ServerInfo> tempList = new CopyOnWriteArrayList<>(threadSafeList);
                    CopyOnWriteArrayList<ServerInfo> missList = new CopyOnWriteArrayList<>(threadSafeList);
                    for(int j = Server._threadSafeList.size()-1; j>=0; j-- ){
                        ServerInfo thisInfo = Server._threadSafeList.get(j);
                        int i;
                        for(i=threadSafeList.size()-1; i>=0; i--){
                            if(threadSafeList.get(i).equals(thisInfo)){
                                missList.remove(i);
                                break;
                            }
                        }
                        if(i == -1){
                            tempList.add(thisInfo);
                        }
                    }

                    // TODO:
                    // Every time a server reboot, they use different port.
                    // Therefore, when we check the ServerInfo, we should skip the port no.

                    // Update nets file
                    Server._threadSafeList = tempList;
                    FileController.writeNets("/tmp/ylin/linda/" + Server.get_name() + "/nets.dat", Server._threadSafeList);

                    // Send the newest nets to the missing server
                    for(ServerInfo serverInfo : missList){
                        P1.addRequest(serverInfo._ipAddr, serverInfo._port, tempList);
                        try {
                            Thread.sleep(200);
                        }catch (InterruptedException e){
                            System.out.print("InterruptedException");
                        }
                    }
                    break;
                case RequestProtocol.OUT:
                    System.out.print("RequestProtocol: OUT\n");
                    Tuple temp = (Tuple)ois.readObject();
                    if(Server._concurrentHashMap.containsKey(temp)){
                        Server._concurrentHashMap.put(temp, Server._concurrentHashMap.get(temp) + 1);
                    }else{
                        Server._concurrentHashMap.put(temp, 1);
                    }
                    break;
                case RequestProtocol.IN:
                    System.out.print("RequestProtocol: IN\n");
                    temp = (Tuple)ois.readObject();
                    if(Server._concurrentHashMap.containsKey(temp)){
                        oos.writeInt(RequestProtocol.HASTUPLE);
                        oos.flush();
                        int ack = ois.readInt();
                        if(ack == RequestProtocol.ACK){
                            if(Server._concurrentHashMap.get(temp) == 1){
                                Server._concurrentHashMap.remove(temp);
                            }else {
                                Server._concurrentHashMap.put(temp, Server._concurrentHashMap.get(temp) - 1);
                            }
                        }else{
                            System.out.print("Some thing wrong.\n");
                        }
                    }else{
                        System.out.print("NOTUPLE\n");
                        oos.writeInt(RequestProtocol.NOTUPLE);
                    }
                    oos.flush();
                    oos.close();
                    ois.close();
                    _clientSocket.close();
                    break;
                case RequestProtocol.RD:
                    System.out.print("RequestProtocol: RD\n");
                    temp = (Tuple)ois.readObject();
                    if(Server._concurrentHashMap.containsKey(temp)){
                        oos.writeInt(RequestProtocol.HASTUPLE);
                    }else{
                        oos.writeInt(RequestProtocol.NOTUPLE);
                    }
                    oos.flush();
                    oos.close();
                    ois.close();
                    _clientSocket.close();
                    break;
            }


        }catch (java.io.IOException e){
            System.out.print("IOException worker " + e + "\n");
        }catch (java.lang.ClassNotFoundException e){
            System.out.print("ClassNotFoundException");
        }
    }
}
