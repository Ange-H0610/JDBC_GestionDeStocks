import java.util.ArrayList;
import java.util.List;

public class Dish {

    private Integer id;
    private String name;
    private Double price;
    private DishTypeEnum dishType;
    private List<DishIngredient> dishIngredients;

    public Dish() {
        this.dishIngredients = new ArrayList<>();
    }

    /* ================= GETTERS & SETTERS ================= */

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    public List<DishIngredient> getDishIngredients() {
        return dishIngredients;
    }

    public void setDishIngredients(List<DishIngredient> dishIngredients) {
        if (dishIngredients == null) {
            this.dishIngredients = new ArrayList<>();
        } else {
            this.dishIngredients = dishIngredients;
            for (DishIngredient di : dishIngredients) {
                di.setDish(this);
            }
        }
    }

    /* ================= LOGIQUE METIER ================= */

    public double getDishCost() {
        double cost = 0;

        for (DishIngredient di : dishIngredients) {
            cost += di.getIngredient().getPrice() * di.getQuantity();
        }

        return cost;
    }

    public double getGrossMargin() {
        if (price == null) {
            return 0;
        }
        return price - getDishCost();
    }

    /* ================= TO STRING ================= */

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", type=" + dishType +
                ", cost=" + getDishCost() +
                ", margin=" + getGrossMargin() +
                '}';
    }
}
