package kopo.newproject.dto;

import java.math.BigDecimal;

public class TotalSpendingDTO {

    private BigDecimal total;

    public TotalSpendingDTO(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
