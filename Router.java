
import java.io.Serializable;
import java.util.HashMap;


public class Router implements Serializable{
    String self;
    String nextHop;
    String destination;
    int cost;
    long current_time;
    static HashMap<String, String> router_ip_map = new HashMap<>();
    static String subnet_mask_octects[] = {"255","255","255","0"};
    
    Router(String self, String nextHop, String destination, int cost){
        this.self = self;
        this.nextHop = nextHop;
        this.destination = destination;
        this.cost = cost;
        this.current_time = System.currentTimeMillis();
        
        router_ip_map.put("queeg", "129.21.30.37");
        router_ip_map.put("comet", "129.21.34.80");
        router_ip_map.put("rhea", "129.21.37.49");
        router_ip_map.put("glados","129.21.22.196");
    }
    Router(){
        router_ip_map.put("queeg", "129.21.30.37");
        router_ip_map.put("comet", "129.21.34.80");
        router_ip_map.put("rhea", "129.21.37.49");
        router_ip_map.put("glados","129.21.22.196");
    }
   
    
    public String getSource(){
        return self;
    }
    
    public int getCost(){
        return cost;
    }
    
    public String getnextHop(){
        return nextHop;
    }
    
    public String getDestination(){
        return destination;
    }
    
    public String getDestinationIP(String destination){
        StringBuffer str = new StringBuffer();
        String ip = router_ip_map.get(destination);
        
        String octects[] = ip.split("\\.");
        
        for(int i=0; i<4; i++){
            int ans = Integer.parseInt(octects[i]) & Integer.parseInt(subnet_mask_octects[i]);
            str.append(ans+".");
            
        }
        return str.substring(0, str.length()-1);
    }
    
    
    public void setSource(String source){
        this.self = source;
    }
    
    public void setCost(int cost){
        this.cost = cost;
    }
    
    public void setnextHop(String nextHop){
        this.nextHop = nextHop;
    }
    
    public void getDestination(String destination){
        this.destination = destination;
    }
    
    public void setCurrentTime(){
        this.current_time = System.currentTimeMillis();
    }
    public long getCurrentTime(){
        return current_time;
    }
    
    public static void main(String args []){
        Router obj = new Router();
        System.out.println(obj.getDestinationIP("glados"));
    
    }
}



/*public void run(){
        
        while (true) {
            if (operation.equals("sender")) {
                System.out.println("Sender");  
                try {
                    datagramSocket_sender = new DatagramSocket();
                    byte[] buffer = "hello".getBytes();
                    InetAddress receiverAddress = InetAddress.getLocalHost();                   
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, receiverAddress, receiver_Port);
                    datagramSocket_sender.send(packet);
                    datagramSocket_sender.close();
                    Thread.sleep(1000);
                } 
                 
                catch (Exception e) {
                    e.printStackTrace();
                }
                    
                }
            
            else {
                    System.out.println("Receiver"); 
                     
                try {
                    if(datagramSocket_receiver!=null){                       
                        byte[] buffer = new byte[datagramSocket_receiver.getReceiveBufferSize()];
                        
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        datagramSocket_receiver.receive(packet);
                        String buffer_str = new String(packet.getData()).trim();
                        System.out.println("message: "+buffer_str);
                    }
                    
                    
                } 
                catch (Exception e) {
                    e.printStackTrace();
                }
                 
                    
            }
            
            
        }
    
    }
    */
    
    /*public void run(){
        while(true){
            if (operation.equals("sender")){
                System.out.println("Sender"); 
                try {
                    for(String neighbor : neighbors_map.keySet()){
                        datagramSocket_sender = new DatagramSocket();
                        InetAddress IPAddress = InetAddress.getByName(neighbor+"cs.rit.edu"); //receiver address
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        ObjectOutputStream os = new ObjectOutputStream(outputStream);
                        os.writeObject(routing_table);
                        byte[] data = outputStream.toByteArray();
                        DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, receiver_Port);
                        datagramSocket_sender.send(sendPacket);
                        
                        os.close();
                        outputStream.close();
                        datagramSocket_sender.close();
                        
                        System.out.println("Message sent from client to "+neighbor);
                    } 
                    
                } 
                catch (Exception e) {
                    e.printStackTrace();
                }              
            }
            else{
                //System.out.println("Receiver"); 
                byte[] incomingData = new byte[1024];
                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                try {
                    datagramSocket_receiver.receive(incomingPacket);
                    byte[] data = incomingPacket.getData();
                    ByteArrayInputStream in = new ByteArrayInputStream(data);
                    ObjectInputStream is = new ObjectInputStream(in);
                    int [][] neighbor_routing_table = (int[][]) is.readObject();
                    System.out.println("Routing table received");
                    displayRoutingTable(neighbor_routing_table);
                    
                } 
                
                catch (Exception e) {
                    e.printStackTrace();
                }
                
            }
        
        
        }
        
    }
    */

/*public static void initialRoutingTable(){
        
        for(int i=0; i<routing_table.length; i++){
            for(int j=0; j<routing_table.length; j++){
                routing_table[i][j] = Integer.MAX_VALUE;
            }
        }
        
        for(String neighbor : neighbors_map.keySet()){
            System.out.println("neighbor = "+neighbor+" "+neighbors_map.get(neighbor));
            int index = server_number_map.get(neighbor);
            routing_table[index][index] = neighbors_map.get(neighbor);
        }
        
        displayRoutingTable(routing_table);
    }
    */  