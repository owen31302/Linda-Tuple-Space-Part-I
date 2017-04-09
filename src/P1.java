import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by owen on 4/7/17.
 */
public class P1 {

    public static void main(String[] args){

        ObjectOutputStream oos;
        ObjectInputStream ois;

        String msg = "Server Start process ... \n";
        System.out.print(msg);

        try{
            ServerSocket s = new ServerSocket(0);
            System.out.println("listening on port: " + s.getLocalPort());
            while(!s.isClosed()){

                Socket clientSocket = s.accept();
                System.out.print("I got a client\n");

                ois = new ObjectInputStream(clientSocket.getInputStream());


                int action = ois.readInt();
                System.out.print("Action: " + action + "\n");
            }
        }catch (IOException e){
            System.out.print("IOException: " + e + "\n" );
        }



    }

}
