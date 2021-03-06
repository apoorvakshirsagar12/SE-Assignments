package stockapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

import com.mvc.util.DBConnection;

@ManagedBean
@SessionScoped
public class StockApiBean {

    private static final long serialVersionUID = 1L;
    static final String API_KEY = "AF93E6L5I01EA39O";

    private String symbol;
    private double price;
    private int qty;
    
    private double amt;
    private String table1Markup;
    private String table2Markup;

    private String selectedSymbol;
    private List<SelectItem> availableSymbols;

    
    
    public String getPurchaseSymbol() {
        if (getRequestParameter("symbol") != null) {
            symbol = getRequestParameter("symbol");
        }
        return symbol;
    }
    
    public void setPurchaseSymbol(String purchaseSymbol) {
        System.out.println("func setPurchaseSymbol()");  //check
    }

    public double getPurchasePrice() {
        if (getRequestParameter("price") != null) {
            price = Double.parseDouble(getRequestParameter("price"));
            System.out.println("price: " + price);
            System.out.println(FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("uid"));
        }
        return price;
    }

    public void setPurchasePrice(double purchasePrice) {
        System.out.println("func setPurchasePrice()");  //check
    }
    
    private String getRequestParameter(String name) {
        return ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getParameter(name);
    }

    @PostConstruct
    public void init() {
        //initially populate stock list
        availableSymbols = new ArrayList<SelectItem>();
        availableSymbols.add(new SelectItem("AAPL", "Apple"));
        availableSymbols.add(new SelectItem("AMZN", "Amazon LLC"));
        availableSymbols.add(new SelectItem("AR", "Antero Resources"));
        availableSymbols.add(new SelectItem("EBAY", "Ebay"));
        availableSymbols.add(new SelectItem("FB", "Facebook, Inc."));
        availableSymbols.add(new SelectItem("GOLD", "Gold"));
        availableSymbols.add(new SelectItem("GOOGL", "Google"));
        availableSymbols.add(new SelectItem("MSFT", "Microsoft"));
        availableSymbols.add(new SelectItem("SLV", "Silver"));
        availableSymbols.add(new SelectItem("TWTR", "Twitter, Inc."));
        availableSymbols.add(new SelectItem("ORCL", "Oracle,Co."));
        availableSymbols.add(new SelectItem("JPM", "JP Morgan,Chase & Co."));

        //initially populate intervals for stock api
        availableIntervals = new ArrayList<SelectItem>();
        availableIntervals.add(new SelectItem("1min", "1min"));
        availableIntervals.add(new SelectItem("5min", "5min"));
        availableIntervals.add(new SelectItem("15min", "15min"));
        availableIntervals.add(new SelectItem("30min", "30min"));
        availableIntervals.add(new SelectItem("60min", "60min"));
    }

    private String selectedInterval;
    private List<SelectItem> availableIntervals;

    public String getSelectedInterval() {
        return selectedInterval;
    }

    public void setSelectedInterval(String selectedInterval) {
        this.selectedInterval = selectedInterval;
    }

    public List<SelectItem> getAvailableIntervals() {
        return availableIntervals;
    }

    public void setAvailableIntervals(List<SelectItem> availableIntervals) {
        this.availableIntervals = availableIntervals;
    }

    public String getSelectedSymbol() {
        return selectedSymbol;
    }

    public void setSelectedSymbol(String selectedSymbol) {
        this.selectedSymbol = selectedSymbol;
    }

    public List<SelectItem> getAvailableSymbols() {
        return availableSymbols;
    }

    public void setAvailableSymbols(List<SelectItem> availableSymbols) {
        this.availableSymbols = availableSymbols;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

   
	public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public double getAmt() {
        return amt;
    }

    public void setAmt(double amt) {
        this.amt = amt;
    }

    public String getTable1Markup() {
        return table1Markup;
    }

  
	public void setTable1Markup(String table1Markup) {
        this.table1Markup = table1Markup;
    }

    public String getTable2Markup() {
        return table2Markup;
    }

    public void setTable2Markup(String table2Markup) {
        this.table2Markup = table2Markup;
    }

    
    public String createMgrRecord(String symbol, double price, int qty, double amt) {
        try {
            Connection conn = DBConnection.createConnection();
            Statement statement = conn.createStatement();
            ResultSet res=null;
            java.sql.Timestamp  sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());
            
            //get userid
            Integer uid = Integer.parseInt((String) FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getSessionMap().get("uid"));
            Integer reqid = Integer.parseInt((String) FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getSessionMap().get("reqId"));
            
            System.out.println(reqid);
            System.out.println("symbol:" + symbol);
            System.out.println("price:" + price);
            System.out.println("qty:" + qty);
            System.out.println("amt:" + amt);
            
            res=statement.executeQuery("select uid from stock_requests_manager where (req_id = '"+reqid+"')");
            res.next();
            int userid=res.getInt("uid");
            
            //check if user has given stock symbol
            res=null;
            res=statement.executeQuery("select * from user_stock where (uid='" + userid + "') and (stock_symbol='"+ symbol +"')");
            if(res.next()==false)//if not
            {
            	System.out.println("no user with given stock");
            	statement.executeUpdate("insert into user_stock (`uid`,`stock_symbol`,`qty`) values ('"+userid+"','"+symbol+"','"+qty+"')");
            	System.out.println("record inserted");
            }
            else//if yes
            {
            	System.out.println("user has given stock");
            	int u=res.getInt("uid");
            	String ss=res.getString("stock_symbol");
            	int qt=res.getInt("qty");
            	System.out.println(u+""+ss+""+qt);
            	int new_qty=qt+qty;
            	PreparedStatement pst=conn.prepareStatement("update user_stock set qty=? where uid=? and stock_symbol=?");
            	pst.setInt(1, new_qty);
            	pst.setInt(2, userid);
            	pst.setString(3, symbol);
            	pst.executeUpdate();
            }
            
            res=null;
            res=statement.executeQuery("select mgt_fees from users where (U_Userid='"+uid+"')");
            res.next();
            double fees=res.getDouble("mgt_fees");
            
            double commission=amt *(fees/100);
            double newAmt=amt+commission;
            statement.executeUpdate("INSERT INTO purchase (`uid`, `stock_symbol`, `qty`, `price`,`amt`,`date`,`action`) "
                    + "VALUES ('" + userid + "','" + symbol + "','" + qty + "','" + price + "','" + newAmt +"','" + sqlDate + "','purchase')");
            res=null;
            res=statement.executeQuery("select balance from users where (U_Userid='" + userid + "')");
            res.next();
            double bal=res.getDouble("balance");
            System.out.println(bal);
            double newBal=bal-newAmt;
            System.out.println(newBal);
            
            PreparedStatement pstmt=conn.prepareStatement("insert into tbl_user(`user_id`,`balance`,`date`) values('"+userid+"','"+newBal+"','"+sqlDate+"')");
			pstmt.executeUpdate();
			
            PreparedStatement pstmt2=conn.prepareStatement("update users set balance=? where U_Userid=?");
            pstmt2.setDouble(1,newBal);
			pstmt2.setInt(2, userid);
			pstmt2.executeUpdate();

			res=null;
            res=statement.executeQuery("select balance from users where (U_Userid='" + uid + "')");
            res.next();
            double balance=res.getDouble("balance");
            System.out.println("mgr balance:"+balance);
            double newBalance=balance+commission;
            System.out.println("new mgr balance"+newBalance);
			
            PreparedStatement pstmt3=conn.prepareStatement("insert into tbl_user(`user_id`,`balance`,`date`) values('"+uid+"','"+newBalance+"','"+sqlDate+"')");
			pstmt3.executeUpdate();
			
            PreparedStatement pstmt4=conn.prepareStatement("update users set balance=? where U_Userid=?");
            pstmt4.setDouble(1,newBalance);
			pstmt4.setInt(2, uid);
			pstmt4.executeUpdate();
			
			PreparedStatement pst=conn.prepareStatement("update stock_requests_manager set symbol=?,price=?,qty=?,date=?,status=? where req_id=?");
			pst.setString(1, symbol);
			pst.setDouble(2, price);
			pst.setInt(3, qty);
			pst.setTimestamp(4, sqlDate);
			pst.setString(5, "approved");
			pst.setInt(6, reqid);
            pst.executeUpdate();
			
            statement.close();
            //conn.close();
            FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully purchased stock",""));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "purchaseManager";
    }
    
    public String createMgrRecord2(String symbol, double price, int qty, double amt) {
        try {
            //System.out.println("symbol: " + this.symbol + ", price: " + this.price + "\n");
            //System.out.println("qty: " + this.qty + ", amt: " + this.amt + "\n");

            Connection conn = DBConnection.createConnection();
            Statement statement = conn.createStatement();
            ResultSet res=null;
            java.sql.Timestamp  sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());
            
            //get userid
            Integer uid = Integer.parseInt((String) FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getSessionMap().get("uid"));
            Integer reqid = Integer.parseInt((String) FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getSessionMap().get("reqId"));
          
            System.out.println(reqid);
            System.out.println("symbol:" + symbol);
            System.out.println("price:" + price);
            System.out.println("qty:" + qty);
            System.out.println("amt:" + amt);
            
            res=statement.executeQuery("select uid from stock_requests_manager where (req_id = '"+reqid+"')");
            res.next();
            int userid=res.getInt("uid");
            
            res=null;
            res=statement.executeQuery("select * from user_stock where (uid='" + userid + "') and (stock_symbol='" + symbol + "')");
            System.out.println("check");
            if(res.next()==false)//if not
            {
            	System.out.println("user doesn't have given stock");
            	FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO, "User doesn't have stock of this category",""));
            	System.out.println("cannot sell");
            }
            else//if yes
            {
            	System.out.println("user has given stock");
            	int u=res.getInt("uid");
            	String ss=res.getString("stock_symbol");
            	int qt=res.getInt("qty");
            	System.out.println(u+""+ss+""+qt);
            	
            	if(qt >= qty)
            	{
            		int new_qty=qt-qty;
                	PreparedStatement pst=conn.prepareStatement("update user_stock set qty=? where uid=? and stock_symbol=?");
                	pst.setInt(1, new_qty);
                	pst.setInt(2, userid);
                	pst.setString(3, symbol);
                	pst.executeUpdate();
                	
                	 res=null;
                     res=statement.executeQuery("select mgt_fees from users where (U_Userid='"+uid+"')");
                     res.next();
                     double fees=res.getDouble("mgt_fees");
                     
                	double commission=amt *(fees/100);
                    
                	statement.executeUpdate("INSERT INTO purchase (`uid`, `stock_symbol`, `qty`, `price`, `amt`,`date`,`action`) "
                            + "VALUES ('" + userid + "','" + symbol + "','" + qty + "','" + price + "','" + amt +"','" + sqlDate + "','sell')");
                	
                    res=null;
                    res=statement.executeQuery("select balance from users where (U_Userid='" + userid + "')");
                    res.next();
                    double bal=res.getDouble("balance");
                    System.out.println(bal);
                    double newBal=(bal+amt)-commission;
                    System.out.println(newBal);
               
                    PreparedStatement pstmt=conn.prepareStatement("insert into tbl_user(`user_id`,`balance`,`date`) values('"+userid+"','"+newBal+"','"+sqlDate+"')");
        			pstmt.executeUpdate();
        			
                    PreparedStatement pstmt2=conn.prepareStatement("update users set balance=? where U_Userid=?");
                    pstmt2.setDouble(1,newBal);
        			pstmt2.setInt(2, userid);
        			pstmt2.executeUpdate();
                    
        			res=null;
                    res=statement.executeQuery("select balance from users where (U_Userid='" + uid + "')");
                    res.next();
                    double balance=res.getDouble("balance");
                    System.out.println(balance);
                    double newBalance=balance+commission;
                    System.out.println(newBalance);
        			
                    PreparedStatement pstmt3=conn.prepareStatement("insert into tbl_user(`user_id`,`balance`,`date`) values('"+uid+"','"+newBalance+"','"+sqlDate+"')");
        			pstmt3.executeUpdate();
        			
                    PreparedStatement pstmt4=conn.prepareStatement("update users set balance=? where U_Userid=?");
                    pstmt4.setDouble(1,newBalance);
        			pstmt4.setInt(2, uid);
        			pstmt4.executeUpdate();
        			
        			PreparedStatement pstm=conn.prepareStatement("update stock_requests_manager set symbol=?,price=?,qty=?,date=?,status=? where req_id=?");
        			pstm.setString(1, symbol);
        			pstm.setDouble(2, price);
        			pstm.setInt(3, qty);
        			pstm.setTimestamp(4, sqlDate);
        			pstm.setString(5, "approved");
        			pstm.setInt(6, reqid);
                    pstm.executeUpdate();
        			
                    statement.close();
                    //conn.close();
                    FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully selled stock",""));
            	}
            	else if(qt < qty)
            	{
            		System.out.println("not enough stocks");
            		FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO, "User doesn't have enough stocks to sell",""));
            		System.out.println("cannot sell");
            	}	
            }
                       
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "purchaseManager";
    }

    
    public String createDbRecord(String symbol, double price, int qty, double amt) {
        try {
            //System.out.println("symbol: " + this.symbol + ", price: " + this.price + "\n");
            //System.out.println("qty: " + this.qty + ", amt: " + this.amt + "\n");

            Connection conn = DBConnection.createConnection();
            Statement statement = conn.createStatement();
            ResultSet res=null;
            java.sql.Timestamp  sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());
            
            //get userid
            Integer uid = Integer.parseInt((String) FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getSessionMap().get("uid"));
          
            System.out.println(uid);
            System.out.println("symbol:" + symbol);
            System.out.println("price:" + price);
            System.out.println("qty:" + qty);
            System.out.println("amt:" + amt);
            
            res=statement.executeQuery("select balance from users where (U_Userid='"+uid+"')");
            res.next();
            double ubal=res.getDouble("balance");
            if(amt>ubal)
            {
            	FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO, "You don't have sufficient balance to purchase the stock of this amount",""));
            }
            else
            {
            //check if user has given stock symbol
            res=null;
            res=statement.executeQuery("select * from user_stock where (uid='" + uid + "') and (stock_symbol='"+ symbol +"')");
            if(res.next()==false)//if not
            {
            	System.out.println("no user with given stock");
            	statement.executeUpdate("insert into user_stock (`uid`,`stock_symbol`,`qty`) values ('"+uid+"','"+symbol+"','"+qty+"')");
            	System.out.println("record inserted");
            }
            else//if yes
            {
            	System.out.println("user has given stock");
            	int u=res.getInt("uid");
            	String ss=res.getString("stock_symbol");
            	int qt=res.getInt("qty");
            	System.out.println(u+""+ss+""+qt);
            	int new_qty=qt+qty;
            	PreparedStatement pst=conn.prepareStatement("update user_stock set qty=? where uid=? and stock_symbol=?");
            	pst.setInt(1, new_qty);
            	pst.setInt(2, uid);
            	pst.setString(3, symbol);
            	pst.executeUpdate();
            }
            statement.executeUpdate("INSERT INTO purchase (`uid`, `stock_symbol`, `qty`, `price`,`amt`,`date`,`action`) "
                    + "VALUES ('" + uid + "','" + symbol + "','" + qty + "','" + price + "','" + amt +"','" + sqlDate + "','purchase')");
            res=null;
            res=statement.executeQuery("select balance from users where (U_Userid='" + uid + "')");
            res.next();
            double bal=res.getDouble("balance");
            System.out.println(bal);
            double newBal=bal-amt;
            System.out.println(newBal);
            String query="insert into tbl_user(`user_id`,`balance`,`date`) values('"+uid+"','"+newBal+"','"+sqlDate+"')";
            PreparedStatement pstmt=conn.prepareStatement(query);
			pstmt.executeUpdate();
			String query2="update users set balance=? where U_Userid=?";
            PreparedStatement pstmt2=conn.prepareStatement(query2);
            pstmt2.setDouble(1,newBal);
			pstmt2.setInt(2, uid);
			pstmt2.executeUpdate();
            
            statement.close();
            //conn.close();
            FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully purchased stock",""));
        }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return "purchase";
    }
    
    public String createDbRecord2(String symbol, double price, int qty, double amt) {
        try {
            //System.out.println("symbol: " + this.symbol + ", price: " + this.price + "\n");
            //System.out.println("qty: " + this.qty + ", amt: " + this.amt + "\n");

            Connection conn = DBConnection.createConnection();
            Statement statement = conn.createStatement();
            ResultSet res=null;
            java.sql.Timestamp  sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());
            
            //get userid
            Integer uid = Integer.parseInt((String) FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getSessionMap().get("uid"));
          
            System.out.println(uid);
            System.out.println("symbol:" + symbol);
            System.out.println("price:" + price);
            System.out.println("qty:" + qty);
            System.out.println("amt:" + amt);
            
            res=statement.executeQuery("select * from user_stock where (uid='" + uid + "') and (stock_symbol='" + symbol + "')");
            System.out.println("check");
            if(res.next()==false)//if not
            {
            	System.out.println("user doesn't have given stock");
            	FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO, "You don't have stock of this category",""));
            	System.out.println("cannot sell");
            }
            else//if yes
            {
            	System.out.println("user has given stock");
            	int u=res.getInt("uid");
            	String ss=res.getString("stock_symbol");
            	int qt=res.getInt("qty");
            	System.out.println(u+""+ss+""+qt);
            	
            	if(qt >= qty)
            	{
            		int new_qty=qt-qty;
                	PreparedStatement pst=conn.prepareStatement("update user_stock set qty=? where uid=? and stock_symbol=?");
                	pst.setInt(1, new_qty);
                	pst.setInt(2, uid);
                	pst.setString(3, symbol);
                	pst.executeUpdate();
                	
                	statement.executeUpdate("INSERT INTO purchase (`uid`, `stock_symbol`, `qty`, `price`, `amt`,`date`,`action`) "
                            + "VALUES ('" + uid + "','" + symbol + "','" + qty + "','" + price + "','" + amt +"','" + sqlDate + "','sell')");
                    res=null;
                    res=statement.executeQuery("select balance from users where (U_Userid='" + uid + "')");
                    res.next();
                    double bal=res.getDouble("balance");
                    System.out.println(bal);
                    double newBal=bal+amt;
                    System.out.println(newBal);
                    String query="insert into tbl_user(`user_id`,`balance`,`date`) values('"+uid+"','"+newBal+"','"+sqlDate+"')";
                    PreparedStatement pstmt=conn.prepareStatement(query);
        			pstmt.executeUpdate();
                    String query2="update users set balance=? where U_Userid=?";
                    PreparedStatement pstmt2=conn.prepareStatement(query2);
                    pstmt2.setDouble(1,newBal);
        			pstmt2.setInt(2, uid);
        			pstmt2.executeUpdate();
                    
                    statement.close();
                    //conn.close();
                    FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully selled stock",""));
            	}
            	else if(qt < qty)
            	{
            		System.out.println("not enough stocks");
            		FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO, "You don't have enough stocks to sell",""));
            		System.out.println("cannot sell");
            	}	
            }
                       
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "purchase";
    }
       
    public void installAllTrustingManager() {
        TrustManager[] trustAllCerts;
        trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            System.out.println("Exception :" + e);
        }
        return;
    }

    public void timeseries() throws MalformedURLException, IOException {

        installAllTrustingManager();

        //System.out.println("selectedItem: " + this.selectedSymbol);
        //System.out.println("selectedInterval: " + this.selectedInterval);
        String urole = (String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("urole");
        String symbol = this.selectedSymbol;
        String interval = this.selectedInterval;
        String url = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=" + symbol + "&interval=" + interval + "&apikey=" + API_KEY;

        this.table1Markup += "URL::: <a href='" + url + "'>Data Link</a><br>";
        InputStream inputStream = new URL(url).openStream();

        // convert the json string back to object
        JsonReader jsonReader = Json.createReader(inputStream);
        JsonObject mainJsonObj = jsonReader.readObject();
        for (String key : mainJsonObj.keySet()) {
            if (key.equals("Meta Data")) {
                this.table1Markup = null; // reset table 1 markup before repopulating
                JsonObject jsob = (JsonObject) mainJsonObj.get(key);
                this.table1Markup += "<style>#detail >tbody > tr > td{ text-align:center;}</style><b>Stock Details</b>:<br>";
                this.table1Markup += "<table>";
                this.table1Markup += "<tr><td>Information</td><td>" + jsob.getString("1. Information") + "</td></tr>";
                this.table1Markup += "<tr><td>Symbol</td><td>" + jsob.getString("2. Symbol") + "</td></tr>";
                this.table1Markup += "<tr><td>Last Refreshed</td><td>" + jsob.getString("3. Last Refreshed") + "</td></tr>";
                this.table1Markup += "<tr><td>Interval</td><td>" + jsob.getString("4. Interval") + "</td></tr>";
                this.table1Markup += "<tr><td>Output Size</td><td>" + jsob.getString("5. Output Size") + "</td></tr>";
                this.table1Markup += "<tr><td>Time Zone</td><td>" + jsob.getString("6. Time Zone") + "</td></tr>";
                this.table1Markup += "</table>";
            } else {
                this.table2Markup = null; // reset table 2 markup before repopulating
                JsonObject dataJsonObj = mainJsonObj.getJsonObject(key);
                this.table2Markup += "<table class='table table-hover'>";
                this.table2Markup += "<thead><tr><th>Timestamp</th><th>Open</th><th>High</th><th>Low</th><th>Close</th><th>Volume</th></tr></thead>";
                this.table2Markup += "<tbody>";
                int i = 0;
                for (String subKey : dataJsonObj.keySet()) {
                    JsonObject subJsonObj = dataJsonObj.getJsonObject(subKey);
                    this.table2Markup
                            += "<tr>"
                            + "<td>" + subKey + "</td>"
                            + "<td>$" + subJsonObj.getString("1. open") + "</td>"
                            + "<td>$" + subJsonObj.getString("2. high") + "</td>"
                            + "<td>$" + subJsonObj.getString("3. low") + "</td>"
                            + "<td>$" + subJsonObj.getString("4. close") + "</td>"
                            + "<td>" + subJsonObj.getString("5. volume") + "</td>";
                    if (i == 0) {
                    	if(urole.equalsIgnoreCase("user"))
                    	{
                        String path = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
                        this.table2Markup += "<td><a class='btn btn-success' href='" + path + "/faces/purchase.xhtml?symbol=" + symbol + "&price=" + subJsonObj.getString("4. close") + "'>Make Transaction</a></td>";
                    	}
                    	else
                    	{
                    		String path = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
                            this.table2Markup += "<td><a class='btn btn-success' href='" + path + "/faces/purchaseManager.xhtml?symbol=" + symbol + "&price=" + subJsonObj.getString("4. close") + "'>Proceed</a></td>";
                    	}
                    }
                    this.table2Markup += "</tr>";
                    i++;
                }
                this.table2Markup += "</tbody></table>";
            }
        }
        return;
    }

    public void purchaseStock() {
        System.out.println("Calling function purchaseStock()");
        System.out.println("stockSymbol: " + FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("stockSymbol"));
        System.out.println("stockPrice" + FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("stockPrice"));
        return;
    }
}
