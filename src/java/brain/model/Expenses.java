package brain.model;

import java.io.Serializable;
import java.sql.Date;

public class Expenses implements Serializable{
    
    //<editor-fold defaultstate="collapsed" desc="Fields">
    private Long id;
    
    private String title;
    
    private String code;
    
    private Double amount;
    
    private Double description;
    
    private Date time;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    //No argument constructor
    public Expenses() {
    }

    //Find by id constructor
    public Expenses(long id) {
        this.id = id;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Getters And Setters">
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getDescription() {
        return description;
    }

    public void setDescription(Double description) {
        this.description = description;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="To String Method">
    @Override
    public String toString() {
        return "Expenses{" + "id=" + id + ", title=" + title + ", code=" + code + ", amount=" + amount + ", description=" + description + ", time=" + time + '}';
    }
    //</editor-fold>
    
}
