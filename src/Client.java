import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by owen on 4/9/17.
 */
public class Client {
    public static void main(String[] args){
        ObjectOutputStream oos;
        ObjectInputStream ois;

        String msg = "Client Start process ... \n";
        System.out.print(msg);

        try{
            Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            os.writeInt(333);
            os.close();
            socket.close();
        }catch (IOException e){
            System.out.print("IOException: " + e + "\n" );
        }

    }

}
