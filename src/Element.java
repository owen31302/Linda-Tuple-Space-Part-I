/**
 * Created by owen on 4/10/17.
 */
public class Element<T> {
    private int _type;
    private T _data;

    public Element(T in){
        _data = in;


        if(in instanceof Integer){
            _type = DataType.INTEGER;
        }else if (in instanceof Float){
            _type = DataType.FLOAT;
        }else if (in instanceof String){
            _type = DataType.STRING;
        }else {
            System.out.print("System does not support this type of data.\n");
        }
    }

    public int get_type(){
        return _type;
    }
    public void set_data(T in){
        _data = in;
    }
    public T get_data(){
        return _data;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Element)){
            return false;
        }
        if (obj == this){
            return true;
        }
        Element that = (Element)obj;
        if(this._type == that._type && this._data.equals(that._data)){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this._type * this._data.hashCode();
    }
}
