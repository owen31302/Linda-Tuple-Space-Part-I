import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by owen on 4/10/17.
 */
public class Tuple implements Serializable{

    private String[] _list;
    private String _str;
    private ArrayList<Integer> _qLocations = new ArrayList<>();

    public void set_list(String[] list){
        _list = list;
        constructString();
    }
    public String[] get_list(){
        return _list;
    }
    public String get_str(){
        return _str;
    }
    public ArrayList<Integer> get_qLocations(){
        return _qLocations;
    }

    private void constructString(){
        StringBuilder temp = new StringBuilder();
        temp.append("(" + _list[0]);
        for (int i = 1; i<_list.length; i++){
            temp.append(", " + _list[i]);
        }
        temp.append(")");
        _str = temp.toString();
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Tuple)){
            return false;
        }
        if (obj == this){
            return true;
        }
        Tuple that = (Tuple)obj;
        if(this._str.equals(that._str)){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this._str.hashCode();
    }
}
