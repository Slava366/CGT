package customer;

import java.io.Serializable;

public class ProductRequest extends Product implements Serializable {

    private String customerName;

    private int amount;


    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
