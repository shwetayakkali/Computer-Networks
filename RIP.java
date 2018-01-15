/* 
 * RIP.java 
 * 
 * Version: 1.1
 *     
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/* 
 * RIP implements the Distance Vector Routing Algorithm and computes shortest path to each node in the network.
 * Handles Failure by re-directing the path.
 */
public class RIP implements Runnable{
    
    String operation;
    static int sender_Port = 9000;
    static int receiver_Port = 9001;
    static DatagramSocket datagramSocket_receiver;
    static DatagramSocket datagramSocket_sender;
    static HashMap<String,String> server_ip_map = new HashMap<>();
    static HashMap<String, Integer> neighbors_map = new HashMap<>();
    static ArrayList<Router> RouterTableList = new ArrayList<>();
    static HashMap<String, Boolean> failure_status_map = new HashMap<>();
    
    public RIP(String operation) {
        this.operation = operation;
    }

    public RIP() throws SocketException {
        server_ip_map.put("queeg", "129.21.30.37");
        server_ip_map.put("comet", "129.21.34.80");
        server_ip_map.put("rhea", "129.21.37.49");
        server_ip_map.put("glados", "129.21.22.196");
        
        datagramSocket_receiver = new DatagramSocket(receiver_Port);
    }
    
    @Override
    public void run(){
   /* 
    * run() handles the three threads i.e. sender, receiver and failure.
    */
        while(true){
            if (operation.equals("sender")){
                                 
                        try {
                            Thread.sleep(2000);
                            
                            sendRoutingTable();
                            
                            
                        } 
                        catch (Exception e) {
                            e.printStackTrace();
                        }              
                
            }
            else if (operation.equals("receiver")){
                    
                    receiveRoutingTable();
                        
                       
               
            }
            
            else if (operation.equals("failure")){
                //failure thread
                
                InetAddress currentAddress = null;                  
                try {
                    currentAddress = InetAddress.getLocalHost();
                } 
                catch (UnknownHostException ex) {
                    ex.printStackTrace();
                }
                synchronized(RouterTableList){
                for( Router current_router_obj: RouterTableList){
                    
                    if( !current_router_obj.getDestination().equals(currentAddress.getHostName()) ){ //&& !failure_status_map.get(current_router_obj.getDestination()) ) {
                    //if( !current_router_obj.getnextHop().equals(currentAddress.getHostName()) && !failure_status_map.get(current_router_obj.getnextHop()) ) {
                        if( ( System.currentTimeMillis() - current_router_obj.getCurrentTime() )> 10000 ){
                            System.out.println("\n");                        
                            System.out.println("\n\n************************ " + current_router_obj.getDestination() + " NODE FAILURE************************\n\n");
                            System.out.println("\n");
                            
                            failure_status_map.put(current_router_obj.getDestination(), true);              // failed once don't send triggered update again
                            current_router_obj.setCurrentTime();                                            // update as it failed
                            
                            updateRoutingTableOnFailure(current_router_obj.getDestination());
                            
                            System.out.println(" UPDATED TABLE ON FAILURE OF NODE ");
                            displayRoutingTable();
                            try {
                                System.out.println("\n************************SENDING TRIGGERED UPDATE************************\n");
                                sendTriggeredUpdate();

                            } 
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            //break;
                            }      
                        }
                    }
                }
            
            }
        }
    }
    
    public static void sendRoutingTable() throws SocketException, UnknownHostException, IOException{
   /* 
    * sendRoutingTable() sends routing table to neighbors.
    */
        synchronized(RouterTableList){
        for(String neighbor : neighbors_map.keySet()){
            datagramSocket_sender = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName(neighbor+".cs.rit.edu"); //receiver address
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            //synchronized(RouterTableList){

            os.writeObject(RouterTableList);
            byte[] data = outputStream.toByteArray();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, receiver_Port);
            datagramSocket_sender.send(sendPacket);

            os.close();
            outputStream.close();
            datagramSocket_sender.close();
            
            System.out.println("Routing table sent to --->   "+neighbor);
            }
             
        } 
    
    }
    
    public static void receiveRoutingTable(){
   /* 
    * receiveRoutingTable() receives routing table and does the necessary computation.
    */
        byte[] incomingData = new byte[1024];
        DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
        try {
            datagramSocket_receiver.receive(incomingPacket);
            if(datagramSocket_receiver != null){
                byte[] data = incomingPacket.getData();
                ByteArrayInputStream in = new ByteArrayInputStream(data);
                ObjectInputStream is = new ObjectInputStream(in);
                ArrayList<Router> neighbor_routing_table = (ArrayList<Router>) is.readObject();
                
                synchronized(RouterTableList){  

                computeRoutingTable(neighbor_routing_table);

                System.out.println("Displaying Routing table at current host :");
                displayRoutingTable();

                }
            }
        } 

        catch (Exception e) {
            e.printStackTrace();
        }
    
    }
            
    public static void displayNeighborTable(ArrayList<Router> neighbor_routing_table){
   /* 
    * displayNeighborTable() display the existing Router Table at the current Host.
    */
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------");
        
        for( Router current_router_obj: neighbor_routing_table){
            System.out.print("Source = "+current_router_obj.getSource());
            //System.out.print("Source = " + Router.router_ip_map.get(current_router_obj.getSource()));
            System.out.print(" | Next Hop = "+current_router_obj.getnextHop());
            //System.out.print(" | Next Hop = "+ Router.router_ip_map.get(current_router_obj.getnextHop()));
            System.out.print(" | Destination = "+current_router_obj.getDestination());
            //System.out.print(" | Destination = "+current_router_obj.getDestinationIP(current_router_obj.getDestination()));
            System.out.print(" | Cost = "+current_router_obj.getCost());
            System.out.print(" | Last Updated = "+current_router_obj.getCurrentTime());
            System.out.println();
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------");
    }
    
    
    public static void updateRoutingTableOnFailure(String failure_host){
   /* 
    * updateRoutingTableOnFailure() sets the cost to infinity in the router table where the next hop or destination has failed.
    */
               
        for( Router current_router_obj: RouterTableList){
            if(current_router_obj.getDestination().equalsIgnoreCase(failure_host) || current_router_obj.getnextHop().equalsIgnoreCase(failure_host)){
                current_router_obj.setCost(Integer.MAX_VALUE);          // set cost to infinity on node failure if nexthop or destination is failure node
            }
        
        }
    }
    
    public static void sendTriggeredUpdate() throws UnknownHostException, IOException{
   /* 
    * sendTriggeredUpdate() sends the Triggered Updates.
    */        
        sendRoutingTable();    
    }
    
    
    public static boolean checkIfRouterInstancePresent(Router incoming_router_obj){
   /* 
    * checkIfRouterInstancePresent() checks if destination is present.
    */          
        //System.out.println("Checking if "+incoming_router_obj.getDestination()+" is in the table...");
        for( Router current_router_obj: RouterTableList){
            if(current_router_obj.getDestination().equals(incoming_router_obj.getDestination())){
                return true;
            }
        
        }
        return false;
    }
    
    
    public static Router fetchRouterObject(Router incoming_router_obj){
   /* 
    * fetchRouterObject() fetches thedestination if present.
    */         
        //System.out.println("Fetching "+incoming_router_obj.getDestination()+" from the table...");
        for( Router current_router_obj: RouterTableList){
            if(current_router_obj.getDestination().equals(incoming_router_obj.getDestination())){
                return current_router_obj;
            }
        
        }
        return null;
    }
    
    public static void computeRoutingTable (ArrayList<Router> neighbor_routing_table) throws UnknownHostException{
   /* 
    * computeRoutingTable() computes the Distance Vector Routing Algorithm.
    */          
        InetAddress currentAddress = InetAddress.getLocalHost();
        for( Router current_router_obj: neighbor_routing_table){
            if(current_router_obj.getDestination().equalsIgnoreCase(currentAddress.getHostName()))
                continue;
            boolean present = checkIfRouterInstancePresent(current_router_obj);
            
            if(!present){
                if( neighbors_map.containsKey(current_router_obj.getDestination()) ){
                    
                    if(!current_router_obj.getnextHop().equalsIgnoreCase(currentAddress.getHostName())){
                        current_router_obj.setCost(neighbors_map.get(current_router_obj.getSource()));
                        current_router_obj.setSource(currentAddress.getHostName());
                        current_router_obj.setCurrentTime();
                        RouterTableList.add(current_router_obj);
                    
                    }
                    //TODO --> check: if I am  the next hop to myself set cost to infinity
                    else if(current_router_obj.getnextHop().equalsIgnoreCase(currentAddress.getHostName())){
                        current_router_obj.setCost(Integer.MAX_VALUE);
                        current_router_obj.setCurrentTime();
                        RouterTableList.add(current_router_obj);
                    }
                    System.out.println("IF ---->  Added an instance to routing table in not present but neighbor");
                }
                else if( !neighbors_map.containsKey(current_router_obj.getDestination()) ){
                    String src = current_router_obj.getSource();
                    int incoming_cost = current_router_obj.getCost();
                    if(neighbors_map.containsKey(src)){
                        
                        int next_hop_cost = neighbors_map.get(src);
                        
                        if((incoming_cost + next_hop_cost) < 0)             //check for overflow
                            incoming_cost = Integer.MAX_VALUE;   
                        else
                            incoming_cost = incoming_cost + next_hop_cost;
                        
                        current_router_obj.setCost(incoming_cost);
                        current_router_obj.setnextHop(src);
                        current_router_obj.setSource(currentAddress.getHostName());
                        current_router_obj.setCurrentTime();
                        //RouterTableList.add(current_router_obj);
                        
                    }
                    //check if you need to include this or not // if i am the next hop set cost to infinity
                    if(current_router_obj.getnextHop().equalsIgnoreCase(currentAddress.getHostName())){
                        current_router_obj.setCost(Integer.MAX_VALUE);
                        current_router_obj.setCurrentTime();
                        //RouterTableList.add(current_router_obj);
                    }
                    RouterTableList.add(current_router_obj);
                    System.out.println("ELSE IF ---->  Added an instance to routing table in not present and not neighbor");
                }
                if( !failure_status_map.containsKey(current_router_obj.getDestination()) ){
                    failure_status_map.put(current_router_obj.getDestination(), false);
                }
            }
            else{
                
                System.out.println("Destination "+current_router_obj.getDestination()+" Already present in routing table, coming via " + current_router_obj.getSource());                
                String src = current_router_obj.getSource();
                int incoming_cost = current_router_obj.getCost();
                //System.out.println("INCOMING COST = "+incoming_cost);
                if(neighbors_map.containsKey(src)){
                    int next_hop_cost = neighbors_map.get(src);
                    if((incoming_cost + next_hop_cost) < 0)             //check for overflow
                        incoming_cost = Integer.MAX_VALUE;   
                    else
                        incoming_cost = incoming_cost + next_hop_cost;
                    //System.out.println("ADDING NEXTHOP COST TO INCOMING COST = "+ incoming_cost);                   
                }
                
                Router existing_router_obj = fetchRouterObject(current_router_obj);
                /*if( !neighbors_map.containsKey(current_router_obj.getDestination()) ){
                    existing_router_obj.setCurrentTime();
                }*/
                
                //System.out.println("EXISTING OBJECT COST = "+existing_router_obj.getCost() + "NEXT HOP = "+existing_router_obj.getnextHop());
                if( incoming_cost != Integer.MAX_VALUE ){
                    if( existing_router_obj!= null ){                               // check if and condition is correct                                         // and currentobj's next hop is 
                        if( incoming_cost <= existing_router_obj.getCost() && !existing_router_obj.getnextHop().equalsIgnoreCase(currentAddress.getHostName())){ //and nexthop is not self : elimination count to infinity problem
                            existing_router_obj.setCost(incoming_cost);
                            existing_router_obj.setnextHop(src);
                            existing_router_obj.setCurrentTime();
                            System.out.println("Updated routing table");

                        }

                    }
                }
                //check
                /*else if( incoming_cost == Integer.MAX_VALUE && existing_router_obj.getCost() < incoming_cost && !existing_router_obj.getnextHop().equalsIgnoreCase(currentAddress.getHostName())){
                    System.out.println("incoming cost is INFINITY but existing object cost is LOWER");
                    existing_router_obj.setCurrentTime();
                
                }*/
                
                //if destination found to be infinity               
                else if( incoming_cost == Integer.MAX_VALUE ){    //add condition here )
                    existing_router_obj.setCost(incoming_cost);
                    existing_router_obj.setCurrentTime();
                    System.out.println("Updated routing table (cost = infinity) when node ( already present ) failure found");
                }
                
                if( !neighbors_map.containsKey(current_router_obj.getDestination()) ){
                    
                }
                
            }
        
        }
   
    }
    
    public static void threadInitializations() throws SocketException, InterruptedException{
   /* 
    * threadInitializations() starts the sender, receiver and failure threads.
    */          
        Runnable receiver = new RIP("receiver");
        new Thread(receiver).start();
        
        Thread.sleep(1000);
        Runnable sender = new RIP("sender");
        new Thread(sender).start();
        
        Thread.sleep(5000);
        Runnable failure = new RIP("failure");
        new Thread(failure).start();
        System.out.println("FAILURE THREAD CHECK STARTED!!!!!!!!!!!!!!");
        
    }
    
      
       
    public static void networkConfiguration() throws UnknownHostException, InterruptedException{
   /* 
    * networkConfiguration() prompts the user to enter a desired network topology
    */          
        InetAddress currentAddress = InetAddress.getLocalHost();
        System.out.println("You are connected to : "+currentAddress.getHostName()+ "\nChoose neighbors and enter weights");
        for(String server : server_ip_map.keySet()){
            if(!currentAddress.getHostName().contains(server)){
                System.out.println("Do you want "+server+" as a neighbor Yes/No");
                Scanner sc = new Scanner(System.in);
                String response = sc.nextLine();
                if(response.equalsIgnoreCase("Yes")){
                    System.out.println("Enter weight for edge between "+currentAddress.getHostName()+" and "+server);
                    int weight = sc.nextInt();
                    neighbors_map.put(server,weight);                    
                }
            }
        }        
        System.out.println("Enter a character to continue");
        Scanner sc = new Scanner(System.in);
        String response = sc.nextLine();
        
        // adding self information to routing list
        Thread.sleep(3000);
        Router router_obj = new Router(currentAddress.getHostName(), currentAddress.getHostName(), currentAddress.getHostName(), 0);
        RouterTableList.add(router_obj);
        
        
    }
    
    public static void displayRoutingTable(){
        System.out.println("\n-----------------------------------------------------------------------------------------------------------------------------------------");
        
        for( Router current_router_obj: RouterTableList){
            //System.out.print("Source = "+current_router_obj.getSource());
            System.out.print("Source = " + Router.router_ip_map.get(current_router_obj.getSource()));
            //System.out.print(" | Next Hop = "+current_router_obj.getnextHop());
            System.out.print(" | Next Hop = "+ Router.router_ip_map.get(current_router_obj.getnextHop()));
            //System.out.print(" | Destination = "+current_router_obj.getDestination());
            System.out.print(" | Destination = "+current_router_obj.getDestinationIP(current_router_obj.getDestination()));
            System.out.print(" | Cost = "+current_router_obj.getCost());
            System.out.print(" | Last Updated = "+ (current_router_obj.getCurrentTime()) +" ms");
            System.out.println();
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------\n");
    }
    
    public static void staticNetworkConfiguration() throws UnknownHostException, InterruptedException{
        InetAddress currentAddress = InetAddress.getLocalHost();
        String host = currentAddress.getHostName();
        System.out.println("Host = "+host);
        if(host.equals("queeg")){
            neighbors_map.put("comet",10);
            neighbors_map.put("rhea",1);
        }
        else if(host.equals("comet")){
            neighbors_map.put("queeg",10);
            neighbors_map.put("glados",3);
        }
        else if(host.equals("rhea")){
            neighbors_map.put("queeg",1);
            neighbors_map.put("glados",4);
        }
        else if(host.equals("glados")){
            neighbors_map.put("comet",3);
            neighbors_map.put("rhea",4);
        }
        
        System.out.println("Enter a character to continue");
        Scanner sc = new Scanner(System.in);
        String response = sc.nextLine();
        
        Thread.sleep(3000);
        Router router_obj = new Router(currentAddress.getHostName(), currentAddress.getHostName(), currentAddress.getHostName(), 0);       
        RouterTableList.add(router_obj);
    
    }
    
    public static void main(String args[]) throws InterruptedException, SocketException, UnknownHostException{
   /* 
    * main(), does the network configuration and setup. Initiates the threads.
    */        
        RIP rip_obj = new RIP();
        
        networkConfiguration();
        //staticNetworkConfiguration();
        
        threadInitializations();
               
        while(true){}
        
        
        
    }
  
    
}
