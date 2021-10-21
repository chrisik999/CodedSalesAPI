package brain.model;

import java.io.Serializable;
import java.sql.Date;

public class Receipt implements Serializable {

    //<editor-fold defaultstate="collapsed" desc="Fields">
    private long id;
    
    private String receiptCode;
    
    private double total;
    
    private double discount;
    
    private String business;
    
    private String sales;
    
    private Date date;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    public Receipt() {
    }
   
    //New Receipt Constructor
    public Receipt(String receiptId, double total, double discount, String business, String sales) {
        this.receiptCode = receiptId;
        this.total = total;
        this.discount = discount;
        this.business = business;
        this.sales = sales;
    }
    
    //Get Receipts from database constructors
    public Receipt(long id, String receiptCode, double total, double discount, String business, String sales, Date date) {
        this.id = id;
        this.receiptCode = receiptCode;
        this.total = total;
        this.discount = discount;
        this.business = business;
        this.sales = sales;
        this.date = date;
    }

    public Receipt(String business) {
        this.business = business;
    }

    public Receipt(long id) {
        this.id = id;
    }
    //</editor-fold>
   
    //<editor-fold defaultstate="collapsed" desc="Getters And Setters">
    public long getId() {
        return id;
    }

    public String getReceiptCode() {
        return receiptCode;
    }

    public void setReceiptCode(String receiptCode) {
        this.receiptCode = receiptCode;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double Total) {
        this.total = Total;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public String getSales() {
        return sales;
    }

    public void setSales(String sales) {
        this.sales = sales;
    }
    
    public Date getDate() {
        return date;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="To String Method">
    @Override
    public String toString() {
        return "Receipt{" + "id=" + id + ", receiptId=" + receiptCode + ", Total=" + total + ", discount=" + discount + ", business=" + business + ", sales=" + sales + '}';
    }
    //</editor-fold>
    
}