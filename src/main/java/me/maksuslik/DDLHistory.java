package me.maksuslik;

import java.util.Date;

public class DDLHistory {
    @Id(autoIncremental = false)
    private Integer id;
    private Date applyDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getApplyDate() {
        return applyDate;
    }

    public void setApplyDate(Date applyDate) {
        this.applyDate = applyDate;
    }

}
