/**
 * Created by owen on 4/9/17.
 */
public class ServerInfo implements java.io.Serializable{
    public int _id;
    public String _name;
    public String _ipAddr;
    public int _port;

    ServerInfo(int id, String name, String ipAddr, int port){
        _id = id;
        _name = name;
        _ipAddr = ipAddr;
        _port = port;
    }

    ServerInfo(String ipAddr, int port){
        _ipAddr = ipAddr;
        _port = port;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ServerInfo)){
            return false;
        }
        if (obj == this){
            return true;
        }
        ServerInfo that = (ServerInfo)obj;
        if( this._id == that._id && this._name.equals(that._name) && this._ipAddr.equals(that._ipAddr) && this._port==that._port){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this._id * this._name.hashCode() * this._ipAddr.hashCode() * this._port;
    }
}
