package provider;

import java.io.Serializable;

public class MaterialRequest extends Material implements Serializable {

    private String providerName;

    private int amount;


    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
