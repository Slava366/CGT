package response;


import provider.Material;

public class StockMaterial extends Material {

    private int amount;


    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
