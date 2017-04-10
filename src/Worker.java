import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
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
            // Receive the other one's nets file.
            ObjectInputStream ois = new ObjectInputStream(_clientSocket.getInputStream());
            int action = ois.readInt();
            System.out.print("RequestProtocol: " + action + "\n");
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

            // 因為每次server boot 起來就會使用不同的port，所以check的時候要省略port才對
            // 想想看class比對的地方要不要把port省略掉。
            // Server info裡面竟然有null，要debug。

            // Update nets file
            Server._threadSafeList = tempList;


            // Send the newest nets to the missing server
            for(ServerInfo serverInfo : missList){
                P1.addRequest(serverInfo._ipAddr, serverInfo._port, tempList);
            }

        }catch (java.io.IOException e){
            System.out.print("IOException");
        }catch (java.lang.ClassNotFoundException e){
            System.out.print("ClassNotFoundException");
        }
    }
}
