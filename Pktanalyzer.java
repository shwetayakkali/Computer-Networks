/* 
 * Pktanalyzer.java 
 * 
 * Version: 1.1
 *     
 */
import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/* 
 * Pktanalyzer has methods for breaking down the header at each stage and display the various header components.
 *     
 */
public class Pktanalyzer {
    private static List <String> bin_to_hex_arr = new ArrayList<>();
    private static String ethernet_arr [] = new String [14];
    private static String ip_arr [] = new String [20];
    private static String protocol_arr [];
    private static String protocol = "";
    
    
   /* 
    * hexToBinary() converts hexadecimal string to binary
    */
    private static String hexToBinary(String hex){
        int number = Integer.parseInt(hex,16);
        return String.format("%8s", Integer.toBinaryString(number)).replace(' ', '0');
    }
    
   /* 
    * hexToDecimal() converts hexadecimal string to decimal number
    */
    private static BigInteger hexToDecimal(String hex){
        return new BigInteger(hex, 16);
    
    }
   /* 
    * getHostName() maps the ip address to a hostname if found else returns Unknown Host.
    */
    private static String getHostName(String ipaddress){
        String host = "";
        try {
            InetAddress addr = InetAddress.getByName(ipaddress);
            host = addr.getHostName();
            host = addr.getCanonicalHostName();
            
        } 
        catch (UnknownHostException ex) {
            return " ( Unknown Host )";            
        }
        if (host.equals(ipaddress)){
            return " ( Unknown Host )";  
        }
        return " ( "+host+" )";
    
    }
   
   /* 
    * TCPHeaderDecode() breaks the TCP header and analyzes the components and displays to the user.
    */
    private static void TCPHeaderDecode(){
        
        System.out.println("TCP:  ----- TCP Header -----");
        System.out.println("TCP:");
        System.out.println("TCP: Source Port = "+ hexToDecimal(protocol_arr[0]+protocol_arr[1]));
        System.out.println("TCP: Destination Port = "+ hexToDecimal(protocol_arr[2]+protocol_arr[3]));
        System.out.println("TCP: Sequence Number = "+ hexToDecimal(protocol_arr[4]+protocol_arr[5]+protocol_arr[6]+protocol_arr[7]));
        System.out.println("TCP: Acknowledgement = "+ hexToDecimal(protocol_arr[8]+protocol_arr[9]+protocol_arr[10]+protocol_arr[11]));
        System.out.println("TCP: Data offset = "+hexToDecimal(protocol_arr[12].charAt(0)+"").intValue()*4);
        int data_offset = hexToDecimal(protocol_arr[12].charAt(0)+"").intValue()*4;
        String convert = hexToBinary(protocol_arr[12].charAt(1)+"").substring(4);
        System.out.println("TCP: Reserved = "+convert.substring(0,3));
        System.out.println("TCP: Nonce = "+convert.substring(3));
        convert = hexToBinary(protocol_arr[13]);
        System.out.println("TCP: CWR = "+convert.charAt(0));
        System.out.println("TCP: ECE = "+convert.charAt(1));
        System.out.println("TCP: URG = "+convert.charAt(2));
        System.out.println("TCP: ACK = "+convert.charAt(3));
        System.out.println("TCP: PSH = "+convert.charAt(4));
        System.out.println("TCP: PST = "+convert.charAt(5));
        System.out.println("TCP: SYN = "+convert.charAt(6));
        System.out.println("TCP: FIN = "+convert.charAt(7));
        System.out.println("TCP: Window Size = "+hexToDecimal(protocol_arr[14]+protocol_arr[15]));
        System.out.println("TCP: Checksum = "+protocol_arr[16]+protocol_arr[17]);
        System.out.println("TCP: Urgent Pointer = "+hexToDecimal(protocol_arr[18]+protocol_arr[19]));
        if(data_offset==20){
            System.out.println("TCP: No Options");
        }
        else{
            System.out.println("TCP: Options Present");
        }
        System.out.println("TCP:");
        System.out.println("TCP: Data: ");
        for(int i=20;i< protocol_arr.length;i++){
            System.out.print(protocol_arr[i]+" ");
        }
       
    }
    
   /* 
    * CMPHeaderDecode() breaks the ICMP header and analyzes the components and displays to the user.
    */
    private static void ICMPHeaderDecode(){
        System.out.println("ICMP:  ----- ICMP Header -----");
        System.out.println("ICMP:");
        System.out.println("ICMP: Type = "+hexToDecimal(protocol_arr[0]));
        System.out.println("ICMP: Code = "+hexToDecimal(protocol_arr[1]));
        System.out.println("ICMP: Checksum = "+protocol_arr[2]+protocol_arr[3]);
        System.out.println("ICMP: BE Identifier = "+protocol_arr[4]+protocol_arr[5]);
        System.out.println("ICMP: LE Identifier = "+protocol_arr[6]+protocol_arr[7]);
        System.out.println("ICMP: Sequence number BE = "+hexToDecimal(protocol_arr[8]+protocol_arr[9]));
        System.out.println("ICMP: Sequence number LE = "+hexToDecimal(protocol_arr[10]+protocol_arr[11]));
        System.out.println("ICMP:");
        System.out.println("ICMP: Data:  ");
        for(int i=13;i<protocol_arr.length; i++){
            System.out.print(protocol_arr[i]+" ");
        }

    }
    
   /* 
    * UDPHeaderDecode() breaks the UDP header and analyzes the components and displays to the user.
    */
    
    private static void UDPHeaderDecode(){
        System.out.println("UDP:  ----- UDP Header -----");
        System.out.println("UDP:");
        System.out.println("UDP: Source Port = "+ hexToDecimal(protocol_arr[0]+protocol_arr[1]));
        System.out.println("UDP: Destination Port = "+ hexToDecimal(protocol_arr[2]+protocol_arr[3]));
        System.out.println("UDP: Length = "+hexToDecimal(protocol_arr[4]+protocol_arr[5]));
        System.out.println("UDP: Checksum = "+protocol_arr[6]+protocol_arr[7]);
        System.out.println("UDP:");
        System.out.println("UDP: Data:  ");
        for(int i=8;i<protocol_arr.length; i++){
            System.out.print(protocol_arr[i]+" ");
        }
        
        
    }
    
   /* 
    * IPHeaderDecode() breaks the IP header and analyzes the components and displays to the user.
    */
    
    private static void IPHeaderDecode(){
        String convert = "";
        System.out.println("IP:  ----- IP Header ----- ");
        System.out.println("IP:");
        System.out.println("IP: Version = "+ip_arr[0].charAt(0));
        System.out.println("IP: Header Length = "+Integer.parseInt(ip_arr[0].charAt(1)+"")*4);
        System.out.println("IP: Type of Service = 0x"+ip_arr[1]);
        convert = hexToBinary(ip_arr[1]);
        System.out.println("IP:      xxx. .... = "+"0"+" (precedence)");
        System.out.println("IP:      ..."+convert.charAt(3)+". .... = normal delay");
        System.out.println("IP:      .... "+convert.charAt(4)+"... = normal thoroughput");
        System.out.println("IP:      .... ."+convert.charAt(5)+".. = normal reliability");
        convert = ip_arr[2]+ip_arr[3];
        System.out.println("IP: Total Length = "+hexToDecimal(convert));
        convert = ip_arr[4]+ip_arr[5];
        System.out.println("IP: Identification = "+hexToDecimal(convert));
        
        convert = hexToBinary(ip_arr[6]) + hexToBinary(ip_arr[7]);
        System.out.println("IP: Flags = "+convert);
        System.out.println("          "+convert.charAt(0)+"... .... = not used (reserved)");
        System.out.println("          ."+convert.charAt(1)+".. .... = do not fragment");
        System.out.println("          .."+convert.charAt(2)+". .... = fragment flag");
        int decimal = Integer.parseInt(convert.substring(4),2);
        convert = Integer.toString(decimal*4,16);
        System.out.println("IP: Fragment Offset = "+convert+" bytes");
        System.out.println("IP: Time to live = "+hexToDecimal(ip_arr[8])+" seconds/hops");
        
        int protocol_no = hexToDecimal(ip_arr[9]).intValue();
        
        if(protocol_no == 6){
            protocol = "TCP";
        }
        else if (protocol_no == 17){
            protocol = "UDP";
        }
        else {
            protocol = "ICMP";
        }
        System.out.println("IP: Protocol = "+hexToDecimal(ip_arr[9])+" ( "+protocol+" )");
        System.out.println("IP: Header Checksum = "+ip_arr[10]+ip_arr[11]);
        String ip_address = hexToDecimal(ip_arr[12])+"."+hexToDecimal(ip_arr[13])+"."+hexToDecimal(ip_arr[14])+"."+hexToDecimal(ip_arr[15]);
        System.out.println("IP: Source Address = "+ip_address+getHostName(ip_address));
        ip_address = hexToDecimal(ip_arr[16])+"."+hexToDecimal(ip_arr[17])+"."+hexToDecimal(ip_arr[18])+"."+hexToDecimal(ip_arr[19]);
        System.out.println("IP: Destination Address = "+ip_address+getHostName(ip_address));
        System.out.println("IP: No options");
        System.out.println("IP:");
    }
    
   /* 
    * ethernetHeaderDecode() breaks the Ethernet header and analyzes the components and displays to the user.
    */
    
    private static void ethernetHeaderDecode(){
        System.out.println("ETHER:  ----- Ether Header ----- ");
        System.out.println("ETHER:");
        System.out.println("ETHER: Destination = "+ethernet_arr[0]+":"+ethernet_arr[1]+":"+ethernet_arr[2]+":"+ethernet_arr[3]+":"+ethernet_arr[4]+":"+ethernet_arr[5]);
        System.out.println("ETHER: Source = "+ethernet_arr[6]+":"+ethernet_arr[7]+":"+ethernet_arr[8]+":"+ethernet_arr[9]+":"+ethernet_arr[10]+":"+ethernet_arr[11]);
        System.out.println("ETHER: Ethernet Type = " + ethernet_arr[12] + ethernet_arr[13]+" (IP)");
        System.out.println("ETHER:");
        
    }
    
   /* 
    * splitIntoArrays() splits the converted hexadecimal bytes array into ethernet array, ip array and protocol array.
    */
    
    private static void splitIntoArrays(){
        for(int i = 0; i < 34; i ++){
            if(i<14){
                ethernet_arr[i] = bin_to_hex_arr.get(i);
            }
            else if(i>13 && i<34){
                ip_arr[i-14] = bin_to_hex_arr.get(i);
            }
            
        }
        protocol_arr = new String[bin_to_hex_arr.size() - 34];
        for(int i = 34 ; i < bin_to_hex_arr.size(); i ++){
            protocol_arr[i-34] = bin_to_hex_arr.get(i);
        
        }
        
    }
    
   /* 
    * binaryFileToHex() converts binary file given as command line argument into hexadecimal bytes.
    */
    
    
    private static void binaryFileToHex(String args[]) throws FileNotFoundException, IOException{
        //String input_file = "C:\\Users\\Shweta Yakkali\\Documents\\NetBeansProjects\\FCN\\src\\new_icmp_packet2.bin";
            try{
            String input_file = args[0];
            FileInputStream in = new FileInputStream(input_file);
            int read;

            while((read = in.read()) != -1){
                String curr_hex = Integer.toHexString(read);
                if(curr_hex.length() == 1){
                    curr_hex = "0"+curr_hex;
                }
                bin_to_hex_arr.add(curr_hex);

            }
            //System.out.println(bin_to_hex_arr +" "+bin_to_hex_arr.size());
            splitIntoArrays();
            }
            catch (FileNotFoundException e){
                System.out.println("File Not Found! Enter the correct filename");
                System.exit(0);
            }
        }
       
    
    public static void main (String args[]) throws FileNotFoundException, IOException{
        if(args.length >0){
            binaryFileToHex(args);        
            ethernetHeaderDecode();
            IPHeaderDecode();
            if(protocol.equals("TCP")){
                TCPHeaderDecode();
            }
            else if(protocol.equals("ICMP")){
                ICMPHeaderDecode();
            }
            else{
                UDPHeaderDecode();
            }
        }
        else{
            System.out.println("Please enter filename in the command.");
        }
    }
}

