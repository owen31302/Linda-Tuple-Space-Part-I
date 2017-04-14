import java.io.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by owen on 4/9/17.
 */
public class FileController {
    public static void setFolder(String path, String mode){
        try{
            File f = new File(path);
            if(f.exists() && f.isDirectory()) {
                System.out.print(path + " already created.\n");
            }else{
                f.mkdir();
                Runtime.getRuntime().exec( "chmod " + mode + " " + f.toPath() );
                System.out.println("DIR created " + path + "");
            }
        }catch (java.io.IOException e){
            System.out.print("IOException\n");
        }
    }

    public static void setFile(String path, String mode){
        try{
            File f = new File(path);
            if(f.exists() && !f.isDirectory()) {
                System.out.print(path + " already created.\n");
            }else{
                f.createNewFile();
                Runtime.getRuntime().exec( "chmod " + mode + " " + f.toPath() );
                System.out.println("File created " + path + "");
            }
        }catch (java.io.IOException e){
            System.out.print("IOException");
        }
    }

    public static void writeNets(String filePath, CopyOnWriteArrayList<ServerInfo> threadSafeList){
        try{
            FileOutputStream fileOut = new FileOutputStream(filePath);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            for(int i = 0; i<threadSafeList.size(); i++){
                out.writeObject(threadSafeList.get(i));
            }
            out.close();
            fileOut.close();
            System.out.print("Finished saving. \n");
        }catch (java.io.FileNotFoundException e){
            System.out.print("FileNotFoundException.\n");
        }catch (java.io.IOException e){
            System.out.print("IOException.\n");
        }
    }

    public static void readNets(String filePath, CopyOnWriteArrayList<ServerInfo> threadSafeList){
        try{
            FileInputStream fileIn = new FileInputStream(filePath);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            while (true){
                ServerInfo serverInfo = (ServerInfo)in.readObject();
                threadSafeList.add(serverInfo);
            }
        }catch (java.lang.ClassNotFoundException e){
            System.out.print("ClassNotFoundException");
        }catch(IOException e){
            // if dat file is empty or finish reading will evoke IOException.
            //System.out.print("IOException inner.\n");
        }
    }

    public static boolean checkHost(int id, String name, String ip, int port, CopyOnWriteArrayList<ServerInfo> threadSafeList){
        if(threadSafeList.size()==0){
            System.out.print("New nets lists.\n");
            threadSafeList.add(new ServerInfo(id, name, ip, port));
            FileController.writeNets("/tmp/ylin/linda/" + Server.get_name() + "/nets.dat", threadSafeList);
            return true;
        }else{
            for(int i = 0; i<threadSafeList.size(); i++){
                if(threadSafeList.get(i)._name.equals(name)){
                    System.out.print("Server name is in existed lists.\n");
                    threadSafeList.get(i)._port = Server.get_port();
                    return true;
                }
            }
        }
        return false;
    }

    public static void printNets(CopyOnWriteArrayList<ServerInfo> threadSafeList){
        for(ServerInfo serverInfo : threadSafeList){
            System.out.print(serverInfo._name + " @ " + serverInfo._ipAddr + " on " + serverInfo._port + "\n");
        }
    }

}
