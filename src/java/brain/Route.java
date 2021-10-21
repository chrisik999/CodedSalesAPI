package brain;

//<editor-fold defaultstate="collapsed" desc="Imports">
import brain.model.Item;
import brain.model.Receipt;
import brain.model.Sale;
import brain.model.StockActivity;
import brain.model.User;
import brain.utils.CodeGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import org.json.JSONObject;
//</editor-fold>

@Path("shop")
public class Route {

    @Context
    private UriInfo context;

    public Route() {
    }
   
    //<editor-fold defaultstate="collapsed" desc="Homepage">
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson() {
        return "Home";
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Login">
    @Path("login")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String  loginUser(@FormParam("p0") String phone, @FormParam("p1") String password){      
        
            JSONObject json = new JSONObject();
            try(Connection dbConn = dbConnect()){
                String sql ="SELECT * FROM codedsales.user WHERE email OR phone = ? ";
                PreparedStatement stmt = dbConn.prepareStatement(sql);
                stmt.setString(1, phone);
                ResultSet rs = stmt.executeQuery();
                if(!rs.next()){
                    json.put("type", "false");
                    json.put("msg", "phone doesn't exist");
                } else{
                    if(password.equals(rs.getString("password"))){
                        User newUser = new User(rs.getString("firstname"), rs.getString("lastname"),rs.getString("email"),rs.getString("phone"),rs.getString("business"),rs.getString("password"));
                        json.put("type", "Success");
                        String user = new Gson().toJson(newUser);
                        json.put("user", user);
                    } else {
                        json.put("type", "false");
                        json.put("msg", "password mismatch");
                    }  
                }  
            }
            catch(Exception ex){
                Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "Error while trying to log in the user", ex);
                json.put("type", "Failed");
                json.put("msg", "internal server error");
            }
        return json.toString();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Get a User">
    @Path("user")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String getUser(@FormParam("p0")String phone){
        JSONObject json = new JSONObject();
        try(Connection dbConn = dbConnect()){
            String sql = "SELECT * FROM codedsales.user WHERE phone=?";
            PreparedStatement stmt = dbConn.prepareStatement(sql);
            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                User user = new User(rs.getString("firstname"), rs.getString("lastname"),rs.getString("email"),rs.getString("phone"),rs.getString("business"),rs.getString("password"));
                json.put("type","success");
                String gson = new Gson().toJson(user);
                json.put("user", gson);
            } else {
                json.put("type", "false");
                json.put("msg", "User not found");
            }
            return json.toString();
        }
        catch(Exception ex){
            Logger.getLogger(Route.class.getName()).log(Level.SEVERE,"Internal server error while trying to get user", ex);
            json.put("type", "Failed");
            json.put("msg", "internal server error");
            return json.toString();
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Get all Users">
    @Path("users")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getUsers(){
        JSONObject json = new JSONObject();
        ArrayList<User> userList = new ArrayList<>();
        try(Connection dbConn = dbConnect()){
            String sql = "SELECT * FROM codedsales.user";
            PreparedStatement stmt = dbConn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                while(rs.next()){
                    
                    User user = new User(rs.getString("firstname"), rs.getString("lastname"),rs.getString("email"),rs.getString("phone"),rs.getString("business"),rs.getString("password"));
                    userList.add(user);
                }
                json.put("type","success");
                String gson = new Gson().toJson(userList);
                json.put("userList", gson);
            } else {
                json.put("type", "false");
                json.put("msg", "No user in database");
            }
            return json.toString();
        }
        catch(Exception ex){
            Logger.getLogger(Route.class.getName()).log(Level.SEVERE,"Internal server error while trying to get all users", ex);
            json.put("type", "Failed");
            json.put("msg", "internal server error");
            return json.toString();
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Create New User">
    @Path("users")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String createUser(@FormParam("phone") String phone, @FormParam("email") String email, @FormParam("password") String password, @FormParam("firstName")String firstName, @FormParam("lastName") String lastName, @FormParam("business") String business){
        JSONObject json = new JSONObject();
        String res = checkUser(email);
        switch(res){
            case "failed": 
                json.put("type", "failed");
                json.put("msg", "Internal Server error");
                break;
            case "true": 
                json.put("type","false");
                json.put("msg","Email already exist");
                break;
            case "false": 
                String resp = checkUser(phone);
                System.out.println("resp"+resp);
                           switch(resp){
                                case "failed": 
                                    json.put("type","failed");
                                    json.put("msg","Internal Server error");
                                    break;
                                case "true": 
                                    json.put("type","false");
                                    json.put("msg","phone already exist");
                                    break;
                                case "false": 
                                    try(Connection dbConn = dbConnect()){
                                        String sql = "INSERT INTO `codedsales`.`user` (`firstname`, `lastname`, `phone`, `email`, `business`, `password`) VALUES (?, ?, ?, ?, ?, ?)";
                                        PreparedStatement stmt = dbConn.prepareStatement(sql);
                                        stmt.setString(1, firstName);
                                        stmt.setString(2, lastName);
                                        stmt.setString(3, phone);
                                        stmt.setString(4, email);
                                        stmt.setString(5, business);
                                        stmt.setString(6, password);
                                        stmt.execute();
                                        json.put("type", "success");
                                        json.put("msg", "success");
                                    }
                                    catch(Exception ex){
                                        Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "Error while trying to log in the user", ex);
                                        json.put("type", "failed");
                                        json.put("msg", "internal server error");
                                    }
                                    break;
                                default: 
                                    json.put("type", "failure");
                                    json.put("msg","An Error Occurred when checking phone");
                                    break;    
                           }
                break;
            default: 
                json.put("type", "failure");
                json.put("msg","An Error Occurred when checking email");
                break;
        }
        return json.toString();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Update a User">
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Delete a User">
    @Path("deleteusers")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteUser(@FormParam("phone") String phone){
        JSONObject json = new JSONObject();
        String res = checkUser(phone);
        switch(res){
            case "failed": 
                json.put("type", "failed");
                json.put("msg", "Internal Server error");
                break;                
            case "false": 
                json.put("type", "false");
                json.put("msg", "User dosen't exist");
                break;
            case "true": 
                try(Connection dbConn = dbConnect()){
                    String sql = "DELETE FROM `codedsales`.`user` WHERE (`phone` = ?)";
                    PreparedStatement stmt = dbConn.prepareStatement(sql);
                    stmt.setString(1, phone);
                    stmt.execute();
                    json.put("type", "success");
                    json.put("msg", "success");
                }
                catch(Exception ex){
                    Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "Internal server error", ex);
                    json.put("type", "failed");
                    json.put("msg", "Internal Server error");
                }
                break; 
            default: 
                json.put("type", "failure");
                json.put("msg", "An Error occurred");
                break;
        }
        return json.toString();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Get all Items">
    @Path("items")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getItems(){
        JSONObject json = new JSONObject();
        ArrayList<Item> itemList = new ArrayList<>();
        try(Connection dbConn = dbConnect()){
            String sql = "SELECT * FROM codedsales.item";
            PreparedStatement stmt = dbConn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                while(rs.next()){
                    Item item = new Item(rs.getLong("id"), rs.getString("name"), rs.getString("code"),rs.getDouble("price"),rs.getString("business"),rs.getString("description"));
                    itemList.add(item);
                }
                json.put("type", "success");
                String items = new Gson().toJson(itemList);
                json.put("itemList", itemList);
            } else {
                json.put("type", "false");
                json.put("msg", "No item found in database");
            }
        }
        catch(Exception ex){
            Logger.getLogger(Route.class.getName()).log(Level.SEVERE,"Internal server error while trying to get all users", ex);
            json.put("type", "failed");
            json.put("msg", "internal server error");
        }
        return json.toString();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Get An Item">
    @Path("getitem")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public  String getAnItem(@FormParam("p0") String code, @FormParam("p1") String business){
        JSONObject json = new JSONObject();
        try(Connection dbConn = dbConnect()){
            String sql ="SELECT * FROM `codedsales`.`item` WHERE `code` = ? AND `business` = ?";
            PreparedStatement stmt = dbConn.prepareStatement(sql);
            stmt.setString(1, code);
            stmt.setString(2, business);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                Item item = new Item(rs.getLong("id"), rs.getString("name"), rs.getString("code"),rs.getDouble("price"),rs.getString("business"),rs.getString("description"));
                json.put("type", "success");
                String items = new Gson().toJson(item);
                json.put("item", items);
            }else {
                json.put("type", "false");
                json.put("msg", "No item found in database");
            }
        }
        catch(Exception ex){
            Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "Internal server error when fetching Item", ex);
            json.put("type", "failed");
            json.put("msg", "internal server error");
        }
        return json.toString();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Create an Item">
    @Path("items")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String createItem(@FormParam("p0") String code, @FormParam("p1") String name, @FormParam("p2") String dprice, @FormParam("p3") String business, @FormParam("p4") String description, @FormParam("p5") String initiator){
        System.out.println(code+" "+name+" "+dprice+" "+business+" "+description+" "+initiator);
        Double price = Double.valueOf(dprice);
        JSONObject json = new JSONObject();
        String res = checkItem(code,business);
        switch(res){
            case "failed":
                json.put("type", "failed");
                json.put("msg", "internal server error while checking item");
                break;
            case "true":
                json.put("type", "false");
                json.put("msg", "item already exist");
                break;
            case "false":
                        try(Connection dbConn = dbConnect()){
                            String sql = "INSERT INTO `codedsales`.`item` (`name`, `business`, `price`, `code`,`description`) VALUES (?, ?, ?, ?,?)";
                            PreparedStatement stmt = dbConn.prepareStatement(sql);
                            stmt.setString(1, name);
                            stmt.setString(2, business);
                            stmt.setDouble(3, price);
                            stmt.setString(4, code);
                            stmt.setString(5, description);
                            stmt.execute();
                            String query = "INSERT INTO `codedsales`.`stock` (`item`, `business`) VALUES (?,?)";
                            PreparedStatement statement = dbConn.prepareStatement(query);
                            statement.setString(1, code);
                            statement.setString(2, business);
                            statement.execute();
                            String result = createStockActivity(initiator, code, business, "Create Item");
                            if(result.equals("success")){
                                json.put("type", "success");
                                json.put("msg", "Successful");
                            } else {
                                json.put("type", "failed");
                                json.put("msg", "internal server erroe");
                            }
                        }
                        catch(Exception ex){
                            Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "internal server error", ex);
                            json.put("type", "failed");
                            json.put("msg", "internal server error");
                        }
                break;
            default:
                json.put("type", "failure");
                json.put("msg", "An error occurred");
                break;
        }
        return json.toString();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Update an Items">
    @Path("updateitem")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String updateItem(@FormParam("p0") String code, @FormParam("p1") String name, @FormParam("p2") String dprice, @FormParam("p3") String business, @FormParam("p4") String description,@FormParam("p5") String initiator){
        Double price = Double.valueOf(dprice);
        JSONObject json = new JSONObject();
        String res = checkItem(code,business);
        switch(res){
            case "failed":
                json.put("type", "failed");
                json.put("msg", "internal server error");
                break;
            case "false":
                json.put("type", "false");
                json.put("msg", "item dosen't exist");
                break;
            case "true":
                try(Connection dbConn = dbConnect()){
                    String sql = "UPDATE `codedsales`.`item` SET `name` = ?, `price` = ?, `description` = ? WHERE (`code` = ?)";
                    PreparedStatement stmt = dbConn.prepareStatement(sql);
                    stmt.setString(1, name);
                    stmt.setDouble(2, price);
                    stmt.setString(3, description);
                    stmt.setString(4, code);
                    stmt.execute();
                }
                catch(Exception ex){
                    Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "internal server error", ex);
                    json.put("type", "failed");
                    json.put("msg", "internal server error");
                }
                String result = createStockActivity(initiator, code, business, "Update Item");
                if(result.equals("success")){
                    json.put("type", "success");
                    json.put("msg", "Successful");
                }
                else{
                    json.put("type", "failed");
                    json.put("msg", "internal server error");
                }
                break;
            default:
                json.put("type", "failure");
                json.put("msg", "An error occurred");
                break;
        }
        return json.toString();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Delete an Items">
    @Path("deleteitems")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteItem(@FormParam("p0") String code, @FormParam("p1") String business,@FormParam("p2") String initiator){
        JSONObject json = new JSONObject();
        String res = checkItem(code,business);
        switch(res){
            case "failed":
                json.put("type", "failed");
                json.put("msg", "internal server error");
                break;
            case "false":
                json.put("type", "false");
                json.put("msg", "Item doesn't exist");
                break;
            case "true":
                try(Connection dbConn = dbConnect()){
                   String sql = "DELETE  FROM `codedsales`.`item` WHERE (`item`.`code` = ? And `item`.`business` = ?)";
                   PreparedStatement stmt = dbConn.prepareStatement(sql);
                   stmt.setString(1, code);
                   stmt.setString(2, business);
                   stmt.execute();
                   String query = "DELETE FROM `codedsales`.`stock` WHERE (`stock`.`item` = ? And `stock`.`business` = ?)";
                   PreparedStatement statement = dbConn.prepareStatement(query);
                   statement.setString(1, code);
                   statement.setString(2, business);
                   statement.execute();
                   String result = createStockActivity(initiator, code, business, "Delete Item");
                   if(result.equals("success")){
                        json.put("type", "success");
                        json.put("msg", "Successful");
                   }else {
                        json.put("type", "false");
                        json.put("msg", "stock activity not created");
                   } 
                }
                catch(Exception ex){
                    Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "internal server error", ex);
                    json.put("type", "failed");
                    json.put("msg", "internal server error"); 
                }
                break;
            default:
                json.put("type", "failure");
                json.put("msg", "An error occurred");
                break;
        }
        return json.toString();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Create Sale">
    @Path("sales")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String makeSale(@FormParam("p0") String description, @FormParam("p1") String initiator, @FormParam("p2") String business, @FormParam("p3") String items, @FormParam("p4") String dtotal, @FormParam("p5") String ddiscount, @FormParam("p6") String damount){
        Double total,amount;
        int discount;
        JSONObject json = new JSONObject(items);
        total = Double.parseDouble(dtotal);
        discount = Integer.parseInt(ddiscount);
        amount = Double.parseDouble(damount);
        Gson gson = new Gson();
        Item [] itemsList = gson.fromJson(json.getString("items"),Item[].class);
        List<Item> itemList = new ArrayList<>();
        Collections.addAll(itemList, itemsList);
        String salesCode = CodeGenerator.generateSalesCode();
        try(Connection dbConn = dbConnect()){
            String sql = "INSERT INTO `codedsales`.`sales` (`description`, `initiator`, `business`, `total`, `discount`, `amount`, `code`) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = dbConn.prepareStatement(sql);
            stmt.setString(1, description);
            stmt.setString(2, initiator);
            stmt.setString(3, business);
            stmt.setDouble(4, total);
            stmt.setInt(5, discount);
            stmt.setDouble(6, amount);
            stmt.setString(7, salesCode);
            stmt.execute();
            //Inserting the sold items into the item_sales table
            String query = "INSERT INTO `codedsales`.`sales_items` (`salecode`, `item`, `quantity`, `unitprice`, `amount`,business) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement qstmt = dbConn.prepareStatement(query);
            for (int i = 0; i < itemList.size(); i++ ){
                Item item = itemList.get(i);
                qstmt.setString(1, salesCode);
                qstmt.setString(2, item.getCode());
                qstmt.setDouble(3, item.getQuantity());
                qstmt.setDouble(4, item.getPrice());
                qstmt.setDouble(5, item.getAmount());
                qstmt.setString(6, business);
                qstmt.execute();
            }
            //updating Inventory... stock table
            String sqlQuery = "UPDATE `codedsales`.`stock` SET `quantity` = quantity - ? WHERE (`item` = ? AND `business` = ?)";
            PreparedStatement statement = dbConn.prepareStatement(sqlQuery);
            for (int i = 0; i < itemList.size(); i++ ){
                Item item = itemList.get(i);
                statement.setDouble(1, item.getQuantity());
                statement.setString(2, item.getCode());
                statement.setString(3, business);
                statement.execute();
            }
            json.put("type", "success");
            json.put("msg", "success");
        }
        catch(Exception ex){
            Logger.getLogger(Route.class.getName()).log(Level.SEVERE, items, ex);
            json.put("type", "failed");
            json.put("msg", "internal server error");
        }
        return json.toString();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Get Daily Sales">
    @Path("getdailysales")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllSales(@FormParam("p0") String phone, @FormParam("p1") String business){
        JSONObject json = new JSONObject();
        ArrayList<Sale> saleList = new ArrayList<>();
        String res = scrutinizeUser(phone, business);
        System.out.println(res);
        switch(res){
            case "Admin":
            case "User":
                try(Connection dbConn = dbConnect()){
                    PreparedStatement stmt;
                    PreparedStatement statement;
                    if(res.equals("Admin")){
                        stmt = dbConn.prepareStatement("SELECT * FROM codedsales.sales where year(now()) = year(date) AND day(now()) = day(date)");
                        //stmt = dbConn.prepareStatement("SELECT *, sum(total) as ftotal FROM codedsales.sales WHERE day(now()) = day(date)");
                        statement =  dbConn.prepareStatement("SELECT sum(total)as total FROM codedsales.sales where year(now()) = year(date) AND day(now()) = day(date)");
                    }
                    else{
                        stmt = dbConn.prepareStatement("SELECT * FROM codedsales.sales where year(now()) = year(date) AND day(now()) = day(date) AND initiator = ?");
                        stmt.setString(1, phone);
                        statement =  dbConn.prepareStatement("SELECT sum(total)as total FROM codedsales.sales where year(now()) = year(date) AND day(now()) = day(date) AND initiator = ?");
                        statement.setString(1, phone);
                    }
            ResultSet rs = stmt.executeQuery();
            ResultSet ans = statement.executeQuery();
            if(!rs.next()){
                json.put("type", "false");
                json.put("msg", "sales table is empty");
            }else {
                do{
                    Sale sale = new Sale(rs.getDouble("total"), rs.getString("business"), rs.getString("initiator"), rs.getInt("discount"), rs.getDouble("amount"), rs.getString("description"), rs.getString("code"), rs.getTimestamp("date"));
                    saleList.add(sale);
                }
                while(rs.next());
                if(!ans.next()){
                    json.put("total", 0D);
                    System.out.println("I came here");
                }
                else{ 
                    Double val = ans.getDouble("total");
                    json.put("total",val.toString() );
                    System.out.println(val.toString());
                }
                json.put("type", "success");
                String gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create().toJson(saleList);
                json.put("saleList", gson);
            }
        }
        catch(Exception ex){
            Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "internal server error", ex);
            json.put("type", "failed");
            json.put("msg", "internal server error");
        }
                break;
            case "false":
                json.put("type", "false");
                json.put("msg", "Invalid Credentials");
                break;
            default:
                json.put("type", "failed");
                json.put("msg", "Internal Server Error");
                break;
    }
        return json.toString();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Get Weeky Sales">
    @Path("getweeklysales")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String getWeeklySales(@FormParam("p0") String phone, @FormParam("p1") String business){
        JSONObject json = new JSONObject();
        ArrayList<Sale> saleList = new ArrayList<>();
        String res = scrutinizeUser(phone, business);
        System.out.println(res);
        switch(res){
            
            case "Admin":
            case "User":
                try(Connection dbConn = dbConnect()){
                    PreparedStatement stmt;
                    PreparedStatement statement;
                    if(res.equals("Admin")){
                        stmt = dbConn.prepareStatement("SELECT * FROM codedsales.sales where year(now()) = year(date) AND week(now()) = week(date)");
                        //stmt = dbConn.prepareStatement("SELECT *, sum(total) as ftotal FROM codedsales.sales WHERE day(now()) = day(date)");
                        statement =  dbConn.prepareStatement("SELECT sum(total)as total FROM codedsales.sales where year(now()) = year(date) AND week(now()) = week(date)");
                    }
                    else{
                        stmt = dbConn.prepareStatement("SELECT * FROM codedsales.sales where year(now()) = year(date) AND week(now()) = week(date) AND initiator = ?");
                        stmt.setString(1, phone);
                        statement =  dbConn.prepareStatement("SELECT sum(total)as total FROM codedsales.sales where year(now()) = year(date) AND weeknow()) = week(date) AND initiator = ?");
                        statement.setString(1, phone);
                    }
            ResultSet rs = stmt.executeQuery();
            ResultSet ans = statement.executeQuery();
            if(!rs.next()){
                json.put("type", "false");
                json.put("msg", "sales table is empty");
            }else {
                do{
                    Sale sale = new Sale(rs.getDouble("total"), rs.getString("business"), rs.getString("initiator"), rs.getInt("discount"), rs.getDouble("amount"), rs.getString("description"), rs.getString("code"), rs.getTimestamp("date"));
                    saleList.add(sale);
                }
                while(rs.next());
                if(!ans.next()){
                    json.put("total", 0D);
                    System.out.println("I came here");
                }
                else{ 
                    Double val = ans.getDouble("total");
                    json.put("total",val.toString() );
                    System.out.println(val.toString());
                }
                json.put("type", "success");
                String gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create().toJson(saleList);
                json.put("saleList", gson);
            }
        }
        catch(Exception ex){
            Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "internal server error", ex);
            json.put("type", "failed");
            json.put("msg", "internal server error");
        }
                break;
            case "false":
                json.put("type", "false");
                json.put("msg", "Invalid Credentials");
                break;
            default:
                json.put("type", "failed");
                json.put("msg", "Internal Server Error");
                break;
    }
        return json.toString();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Get Monthly Sales">
    @Path("getmonthlysales")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String getMonthlySales(@FormParam("p0") String phone, @FormParam("p1") String business){
        JSONObject json = new JSONObject();
        ArrayList<Sale> saleList = new ArrayList<>();
        String res = scrutinizeUser(phone, business);
        System.out.println(res);
        switch(res){
            
            case "Admin":
            case "User":
                try(Connection dbConn = dbConnect()){
                    PreparedStatement stmt;
                    PreparedStatement statement;
                    if(res.equals("Admin")){
                        stmt = dbConn.prepareStatement("SELECT * FROM codedsales.sales where year(now()) = year(date) AND month(now()) = month(date)");
                        //stmt = dbConn.prepareStatement("SELECT *, sum(total) as ftotal FROM codedsales.sales WHERE day(now()) = day(date)");
                        statement =  dbConn.prepareStatement("SELECT sum(total)as total FROM codedsales.sales where year(now()) = year(date) AND month(now()) = month(date)");
                    }
                    else{
                        stmt = dbConn.prepareStatement("SELECT * FROM codedsales.sales where year(now()) = year(date) AND month(now()) = month(date) AND initiator = ?");
                        stmt.setString(1, phone);
                        statement =  dbConn.prepareStatement("SELECT sum(total)as total FROM codedsales.sales where year(now()) = year(date) AND month(now()) = month(date) AND initiator = ?");
                        statement.setString(1, phone);
                        
                        
                    }
            ResultSet rs = stmt.executeQuery();
            ResultSet ans = statement.executeQuery();
            if(!rs.next()){
                json.put("type", "false");
                json.put("msg", "sales table is empty");
            }else {
                do{
                    Sale sale = new Sale(rs.getDouble("total"), rs.getString("business"), rs.getString("initiator"), rs.getInt("discount"), rs.getDouble("amount"), rs.getString("description"), rs.getString("code"), rs.getTimestamp("date"));
                    saleList.add(sale);
                }
                while(rs.next());
                if(!ans.next()){
                    json.put("total", 0D);
                    System.out.println("I came here");
                }
                else{ 
                    Double val = ans.getDouble("total");
                    json.put("total",val.toString() );
                    System.out.println(val.toString());
                }
                json.put("type", "success");
                String gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create().toJson(saleList);
                json.put("saleList", gson);
            }
        }
        catch(Exception ex){
            Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "internal server error", ex);
            json.put("type", "failed");
            json.put("msg", "internal server error");
        }
                break;
            case "false":
                json.put("type", "false");
                json.put("msg", "Invalid Credentials");
                break;
            default:
                json.put("type", "failed");
                json.put("msg", "Internal Server Error");
                break;
    }
        return json.toString();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Delete a Sale...Commented">
//    @Path("deletesale")
//    @POST
//    public String deleteSale(@FormParam("code") String code, @FormParam("business") String business){
//        JSONObject json = new JSONObject();
//        String res = checkSale(code,business);
//        switch(res){
//            case "failed":
//                json.put("type", "failed");
//                json.put("msg", "internal server error");
//                break;
//            case "false":
//                json.put("type", "false");
//                json.put("msg", "Sale doesn't exist");
//                break;
//            case "true":
//                try(Connection dbConn = dbConnect()){
//                   String sql = "DELETE FROM `codedsales`.`sales` WHERE (`code` = ? AND `business` = ?)";
//                   PreparedStatement stmt = dbConn.prepareStatement(sql);
//                   stmt.setString(1, code);
//                   stmt.setString(2, business);
//                   stmt.execute();
//                   json.put("type", "success");
//                   json.put("msg", "success"); 
//                }
//                catch(Exception ex){
//                    Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "internal server error", ex);
//                    json.put("type", "failed");
//                    json.put("msg", "internal server error"); 
//                }
//                break;
//            default:
//                json.put("type", "failure");
//                json.put("msg", "An error occurred");
//                break;
//        }
//        return json.toString();
//    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Add Stock">
    @Path("addstock")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String addItem(@FormParam("p0") String itemcode, @FormParam("p1") String initiator, @FormParam("p2") String dquantity,@FormParam("p3") String business){
        Double quantity = Double.valueOf(dquantity);
        JSONObject json = new JSONObject();
        String checkItem = checkItem(itemcode, business);
        switch(checkItem){
            case "failed":
                json.put("type", "failed");
                json.put("msg", "internal server error");
                break;
            case "false":
                json.put("type", "false");
                json.put("msg", "Item does not exist");
                break;
            case "true":
                try(Connection dbConn = dbConnect()){
                    String sql = "INSERT INTO `codedsales`.`stock_up` (`initiator`, `item`, `quantity`, `business`) VALUES (?,?,?,?)";
                    PreparedStatement stmt = dbConn.prepareStatement(sql);
                    stmt.setString(1, initiator);
                    stmt.setString(2, itemcode);
                    stmt.setDouble(3, quantity);
                    stmt.setString(4, business);
                    String query = "UPDATE `codedsales`.`stock` SET `quantity` = quantity + ? WHERE (`item` = ? AND `business` = ?)";
                    PreparedStatement statement = dbConn.prepareStatement(query);
                    statement.setDouble(1, quantity);
                    statement.setString(2, itemcode);
                    statement.setString(3, business);
                    stmt.execute();
                    statement.execute();
                    String result = createStockActivity(initiator, itemcode, business, "Add stocks of Item");
                    if(result.equals("success")){
                            json.put("type", "success");
                            json.put("msg", "Successful");
                    }else {
                            json.put("type", "false");
                            json.put("msg", "stock activity not created");
                    }
                }
                catch(Exception ex){
                    Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "internal server error", ex);
                    json.put("type", "failed");
                    json.put("msg", "internal server error");
                }
                break;
            default:
                json.put("type", "failure");
                json.put("msg", "An error occurred");
                break;
        }
        return json.toString();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Remove Stock">
    @Path("removestock")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String removeStock(@FormParam("p0") String itemcode, @FormParam("p1") String initiator, @FormParam("p2") String dquantity,@FormParam("p3") String business){
        Double quantity = Double.valueOf(dquantity);
        JSONObject json = new JSONObject();
        String checkItem = checkItem(itemcode, business);
        switch(checkItem){
            case "failed":
                json.put("type", "failed");
                json.put("msg", "internal server error");
                break;
            case "false":
                json.put("type", "false");
                json.put("msg", "Item does not exist");
                break;
            case "true":
                try(Connection dbConn = dbConnect()){
                    String sql = "INSERT INTO `codedsales`.`remove_stock` (`initiator`, `item`, `quantity`, `business`) VALUES (?,?,?,?)";
                    PreparedStatement stmt = dbConn.prepareStatement(sql);
                    stmt.setString(1, initiator);
                    stmt.setString(2, itemcode);
                    stmt.setDouble(3, quantity);
                    stmt.setString(4, business);
                    String query = "UPDATE `codedsales`.`stock` SET `quantity` = quantity - ? WHERE (`item` = ? AND `business` = ?)";
                    PreparedStatement statement = dbConn.prepareStatement(query);
                    statement.setDouble(1, quantity);
                    statement.setString(2, itemcode);
                    statement.setString(3, business);
                    stmt.execute();
                    statement.execute();
                    String result = createStockActivity(initiator, itemcode, business, "Remove stocks of Item");
                    if(result.equals("success")){
                            json.put("type", "success");
                            json.put("msg", "Successful");
                    }else {
                            json.put("type", "false");
                            json.put("msg", "stock activity not created");
                    }
                }
                catch(Exception ex){
                    Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "internal server error", ex);
                    json.put("type", "failed");
                    json.put("msg", "internal server error");
                }
                break;
            default:
                json.put("type", "failure");
                json.put("msg", "An error occurred");
                break;
        }
        return json.toString();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="View All Stock Activities">
    @Path("stockactivity")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getStockActivities(){
        JSONObject json = new JSONObject();
        ArrayList<StockActivity> activityList = new ArrayList<>();
        try(Connection dbConn = dbConnect()){
            String sql = "SELECT * FROM `codedsales`.`stock_activity`";
            PreparedStatement stmt = dbConn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                StockActivity stockActivity = new StockActivity(rs.getInt("id"), rs.getString("initiator"), rs.getString("item"), rs.getString("business"), rs.getString("type"), rs.getDate("date"));
                activityList.add(stockActivity);
            } 
            json.put("type", "success");
            String gson = new Gson().toJson(activityList);
            json.put("activity List", gson);
        }
        catch(Exception ex){
            Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "internal server error", ex);
            json.put("type", "failed");
            json.put("msg", "internal server error");
        }
        return json.toString();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="View Business or User Stock Activities">
    @Path("itemaudit")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String getUserStockActivities(@FormParam ("p0") String phone, @FormParam ("p1") String business){
        JSONObject json = new JSONObject();
        String res = scrutinizeUser(phone, business);
        switch(res){
            case "Admin":
            case "User":
                ArrayList<StockActivity> activityList = new ArrayList<>();
                try(Connection dbConn = dbConnect()){
                    PreparedStatement stmt;
                    if(res.equals("Admin")){
                        stmt = dbConn.prepareStatement("SELECT * FROM `codedsales`.`stock_activity` Where business = ?");
                        stmt.setString(1, business);
                    } else{
                        stmt = dbConn.prepareStatement("SELECT * FROM `codedsales`.`stock_activity` Where business = ? AND initiator = ?");
                        stmt.setString(1, business);
                        stmt.setString(2, phone);
                    }
                    ResultSet rs = stmt.executeQuery();
                    if(!rs.next()){
                        json.put("type", "false");
                        json.put("msg", "Activity List is empty");
                    }
                    else{
                    do{
                        StockActivity stockActivity = new StockActivity(rs.getInt("id"), rs.getString("initiator"), rs.getString("item"), rs.getString("business"), rs.getString("type"), rs.getDate("date"));
                        activityList.add(stockActivity);
                    } while(rs.next());
                    json.put("type", "success");
                    String gson = new Gson().toJson(activityList);
                    json.put("activity List", gson);
                    }
                }
                catch(Exception ex){
                    Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "internal server error", ex);
                    json.put("type", "failed");
                    json.put("msg", "internal server error");
                }
                break;
            case "false":
                json.put("type", "false");
                json.put("msg", "Invalid Credentials");
                break;
            case "failed":
                json.put("type", "failed");
                json.put("msg", "Internal server error");
                break;
            default:
                json.put("type", "failure");
                json.put("msg", "E don happen!!!.");
                break;
        }
        return json.toString();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Create a new Stock Activity">
    @Path("stockactivity")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String createNewStockActivity(@FormParam("initiator") String initiator, @FormParam("item") String item, @FormParam("business") String business, @FormParam("type") String type){
        JSONObject json = new JSONObject();
        String res = createStockActivity(initiator, item, business, type);
        switch(res){
            case "success":
                json.put("type", "success");
                json.put("msg", "success");
                break;
            case "failed":
                json.put("type", "failed");
                json.put("msg", "internal server error");
                break;
            default:
                json.put("type", "failure");
                json.put("msg", "An error occurred");
        }
        return json.toString();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Create a new Stock Activity...Commented">
//    @Path("stockactivity")
//    @POST
//    @Produces(MediaType.APPLICATION_JSON)
//    public String createAStockActivity(@FormParam("initiator") String initiator, @FormParam("item") String item, @FormParam("business") String business, @FormParam("type") String type){
//        JSONObject json = new JSONObject();
//        String rs = scrutinizeUser(initiator, business);
//        switch(rs){
//            case "Admin":
//            case "User":
//                String res = createStockActivity(initiator, item, business, type);
//                switch(res){
//                    case "success":
//                        json.put("type", "success");
//                        json.put("msg", "Successful");
//                        break;
//                    case "failed":
//                        json.put("type", "failed");
//                        json.put("msg", "internal server error");
//                        break;
//                    default:
//                        json.put("type", "failure");
//                        json.put("msg", "An error occurred");
//                        break;
//                }
//                break;
//            case "false":
//                json.put("type", "false");
//                json.put("msg", "Invalid Credentials");
//                break;
//            case "failed":
//                json.put("type", "failed");
//                json.put("msg", "Internal Server Error");
//                break;
//            default:
//                json.put("type", "failure");
//                json.put("msg", "Modaran!!!");
//                break;
//        }
//        return json.toString();
//    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Get all Receipts">
    @Path("receipts")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getReceipts(){
        JSONObject json = new JSONObject();
        ArrayList<Receipt> receiptList = new ArrayList<>();
        try(Connection dbConn = dbConnect()){
            String sql = "SELECT * FROM `codedsales`.`receipt`";
            PreparedStatement stmt = dbConn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                Receipt receipt = new Receipt(rs.getLong("id"), rs.getString("receiptcode"), rs.getDouble("total"), rs.getDouble("discount"), rs.getString("business"),rs.getString("salescode"), rs.getDate("date"));
                receiptList.add(receipt);
            } 
            json.put("type", "success");
            String gson = new Gson().toJson(receiptList);
            json.put("receipt List", gson);
        }
        catch(Exception ex){
            Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "internal server error", ex);
            json.put("type", "failed");
            json.put("msg", "internal server error");
        }
        return json.toString();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Create a Receipt">
    @Path("receipts")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String createReceipt(){
        String code = CodeGenerator.randomString(14);
        return code;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Delete a Receipt">
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Test Your Api">
    @Path("test")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String test() {
        return "Api is working";
    }
    //</editor-fold>
    
    
    
    //<editor-fold defaultstate="collapsed" desc="Below are ReUseable Modules">
    //</editor-fold>
    
    
    //<editor-fold defaultstate="collapsed" desc="Mysql Database Connection">
    public  Connection dbConnect() throws ClassNotFoundException, SQLException{
        String URL = "jdbc:mysql://localhost:3306/codedsales?useSSL=false&serverTimezone=Africa/Lagos";
        String USERNAME = "root";
        String PASSWORD = "Mysql5250";
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Check if User Exist Method">
    public  String checkUser(String email){
        String phone = email;
        try(Connection dbConn = dbConnect()){
            String sql ="SELECT * FROM codedsales.user WHERE `email` = ? OR `phone` = ? ";
            PreparedStatement stmt = dbConn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, phone);
            ResultSet rs = stmt.executeQuery();
            Boolean checkRs = rs.next();
            return checkRs.toString();
        }
        catch(Exception ex){
            Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "Internal server error when checking User", ex);
            return "failed";
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Check if Item Exist Method">
    public  String checkItem(String code, String business){
        try(Connection dbConn = dbConnect()){
            String sql ="SELECT * FROM codedsales.item WHERE `code` = ? AND `business` = ?";
            PreparedStatement stmt = dbConn.prepareStatement(sql);
            stmt.setString(1, code);
            stmt.setString(2, business);
            ResultSet rs = stmt.executeQuery();
            Boolean checkRs = rs.next();
            return checkRs.toString();
        }
        catch(Exception ex){
            Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "Internal server error when checking User", ex);
            return "failed";
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Check if Sale Exist Method">
    private String checkSale(String code, String business) {
        try(Connection dbConn = dbConnect()){
            String sql ="SELECT * FROM codedsales.sales WHERE `code` = ? AND `business` = ?";
            PreparedStatement stmt = dbConn.prepareStatement(sql);
            stmt.setString(1, code);
            stmt.setString(2, business);
            ResultSet rs = stmt.executeQuery();
            Boolean checkRs = rs.next();
            return checkRs.toString();
        }
        catch(Exception ex){
            Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "Internal server error when checking Sale", ex);
            return "failed";
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Create a StockActivity Method">
    public String createStockActivity(String initiator, String item, String business, String type){
        String msg;
        try(Connection dbConn = dbConnect()){
            String sql = "INSERT INTO `codedsales`.`stock_activity` (`initiator`, `item`, `business`, `type`) VALUES (?,?,?,?)";
            PreparedStatement stmt = dbConn.prepareStatement(sql);
            stmt.setString(1, initiator);
            stmt.setString(2, item);
            stmt.setString(3, business);
            stmt.setString(4, type);
            stmt.execute();
            msg = "success"; 
        }
        catch(Exception ex) {
            Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "internal server error", ex);
            msg = "failed";
        }
        return msg;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Scrutinize User">
    private String scrutinizeUser(String phone, String business){
        
        try(Connection dbConn = dbConnect()){
            String sql ="SELECT * FROM codedsales.user WHERE `business` = ? AND `phone` = ? ";
            PreparedStatement stmt = dbConn.prepareStatement(sql);
            stmt.setString(1, business);
            stmt.setString(2, phone);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                String userType = rs.getString("usertype");
            return userType;
            }
            else return "false";
        }
        catch(Exception ex){
            Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "Internal server error when checking User", ex);
            return "failed";
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Check if Code Exist Method">
    public  String checkCode(String code, String tables){
        try(Connection dbConn = dbConnect()){
            String sql ="SELECT * FROM codedsales.? WHERE `code` = ?";
            PreparedStatement stmt = dbConn.prepareStatement(sql);
            stmt.setString(1, tables);
            stmt.setString(2, code);
            ResultSet rs = stmt.executeQuery();
            Boolean checkRs = rs.next();
            return checkRs.toString();
        }
        catch(Exception ex){
            Logger.getLogger(Route.class.getName()).log(Level.SEVERE, "Internal server error when checking User", ex);
            return "failed";
        }
    }
    //</editor-fold>
    
}
