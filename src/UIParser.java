import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by owen on 4/9/17.
 */
public class UIParser {
    public static String cmd(String s){

        if (s.length() >= 3 && s.substring(0, 3).equals("add")){
            return "add";
        }else if ( s.length() >= 3 && s.substring(0, 3).equals("out")){
            return "out";
        }else if ( s.length() >= 2 && s.substring(0, 2).equals("rd")){
            return "rd";
        }else if  ( s.length() >= 2 && s.substring(0, 2).equals("in")){
            return "in";
        }else if  ( s.length() >= "delete".length() && s.substring(0, "delete".length()).equals("delete")){
            return "delete";
        }else{
            return "";
        }
    }

    public static List<ServerInfo> addMultipleParser(String str){
        List<String> matches = get_matches(str, "\\((.*?)\\)");

        List<ServerInfo> result = new ArrayList<>();
        StringBuilder ip;
        StringBuilder port;
        for(String s : matches){
            ip = new StringBuilder();
            port = new StringBuilder();
            if(addParser(s, ip, port)){
                result.add(new ServerInfo(ip.toString(), Integer.parseInt(port.toString())));
            }
        }
        return result;
    }

    public static boolean addParser(String s, StringBuilder ip, StringBuilder port){
        String[] a = s.split(" ");

        // check three parts
        // check ip addr format
        // check port range
        if(a.length == 3){

            String[] subA = a[1].split(".");
            if(subA.length == 4){
                for (String str : subA){
                    int number = Integer.parseInt(str);
                    if(!(number >= 0 && number <= 255)){
                        System.out.print("You have to set addr within 0 to 255.\n");
                        return false;
                    }
                }
            }

            int p = Integer.parseInt(a[2]);
            if(!(p >= 1024 && p <= 65535)){
                System.out.print("You have to set port within 1024 to 65535.\n");
                return false;
            }

            ip.delete(0, ip.length());
            ip.append(a[1]);
            port.delete(0, port.length());
            port.append(a[2]);
            return true;
        }
        return false;
    }

    public static boolean outParser(String str, Tuple tuple){

        /**
         * 1. Find "("
         * 2. Find ")" after "("
         * 3. Split using ","
         * 4. Trim the content
         * 5. Check if content is nothing ie. "", then return false
         * 6. Check String
         * 7. Check Integer
         * 8. Last one will be float
         */

        int leftP = str.indexOf("(");
        if(leftP == -1 ){
            return false;
        }
        int rightP = str.indexOf(")", leftP+1);
        if(rightP == -1){
            return false;
        }

        String subStr = str.substring(leftP+1, rightP);
        String[] subStrArray = subStr.split(",");
        for(int i = 0; i<subStrArray.length; i++){
            subStrArray[i] = subStrArray[i].trim();
            if(subStrArray[i].equals("")){
                return false;
            }else{
                if(!(subStrArray[i].indexOf("\"") != subStrArray[i].lastIndexOf("\""))){
                    try {
                        Integer.parseInt(subStrArray[i]);
                    }catch (NumberFormatException e){
                        try{
                            Float.parseFloat(subStrArray[i]);
                        }catch (NumberFormatException e1){
                            return false;
                        }
                    }
                }
            }
        }

        tuple.set_list(subStrArray);

        return true;
    }

    public static List<String> get_matches(String s, String p) {
        // returns all matches of p in s for first group in regular expression
        List<String> matches = new ArrayList<>();
        Matcher m = Pattern.compile(p).matcher(s);
        while(m.find()) {
            matches.add(m.group(1));
        }
        return matches;
    }
}
