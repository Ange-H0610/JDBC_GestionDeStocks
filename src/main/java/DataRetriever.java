import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    /* ===================== ORDER ===================== */

   public Order findOrderByReference(String reference) {
    Order order = null;
    // MODIFIER LA REQUÊTE SQL
    String sql = """
            SELECT id, reference, creation_datetime, order_type, order_status 
            FROM "order" 
            WHERE reference = ?
            """;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, reference);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            order = new Order();
            int orderId = rs.getInt("id");

            order.setId(orderId);
            order.setReference(rs.getString("reference"));
            order.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());
            
            // NOUVEAUX CHAMPS
            String orderTypeStr = rs.getString("order_type");
            if (orderTypeStr != null) {
                order.setOrderType(OrderType.valueOf(orderTypeStr));
            }
            
            String orderStatusStr = rs.getString("order_status");
            if (orderStatusStr != null) {
                order.setOrderStatus(OrderStatus.valueOf(orderStatusStr));
            }
            
            order.setDishOrderList(findDishOrdersByOrderId(orderId));
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return order;
}
    private List<DishOrder> findDishOrdersByOrderId(int orderId) {
        List<DishOrder> list = new ArrayList<>();
        String sql = "SELECT id, id_dish, quantity FROM dish_order WHERE id_order = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DishOrder dishOrder = new DishOrder();
                dishOrder.setId(rs.getInt("id"));
                dishOrder.setQuantity(rs.getInt("quantity"));
                dishOrder.setDish(findDishById(rs.getInt("id_dish")));
                list.add(dishOrder);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /* ===================== DISH ===================== */

    public Dish findDishById(int id) {
        Dish dish = null;
        String sql = "SELECT id, name, dish_type, selling_price FROM dish WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                dish = new Dish();
                dish.setId(rs.getInt("id"));
                dish.setName(rs.getString("name"));
                dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                dish.setPrice(rs.getDouble("selling_price"));
                dish.setDishIngredients(findIngredientsByDishId(id));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dish;
    }

    private List<DishIngredient> findIngredientsByDishId(int dishId) {
        List<DishIngredient> list = new ArrayList<>();
        String sql = """
                SELECT i.id, i.name, i.price, i.category, di.required_quantity, di.unit
                FROM ingredient i
                JOIN dish_ingredient di ON di.id_ingredient = i.id
                WHERE di.id_dish = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dishId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setPrice(rs.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));

                DishIngredient di = new DishIngredient();
                di.setIngredient(ingredient);
                di.setQuantity(rs.getDouble("required_quantity"));
                di.setUnit(Unit.valueOf(rs.getString("unit")));

                list.add(di);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /* ===================== INGREDIENT ===================== */

    public Ingredient findIngredientById(int id) {
        Ingredient ingredient = null;
        String sql = "SELECT id, name, category, price FROM ingredient WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                ingredient.setPrice(rs.getDouble("price"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ingredient;
    }
}

public Order saveOrder(Order orderToSave) {
    String sql;
    boolean isNewOrder = (orderToSave.getId() == null);
    
    if (isNewOrder) {
       
        sql = """
            INSERT INTO "order" (reference, creation_datetime, order_type, order_status) 
            VALUES (?, ?, ?, ?) 
            RETURNING id
            """;
    } else {
       
        Order existingOrder = findOrderById(orderToSave.getId());
        if (existingOrder != null && existingOrder.getOrderStatus() == OrderStatus.DELIVERED) {
         
            throw new OrderDeliveredException(
                "Impossible de modifier une commande déjà livrée. Référence: " + 
                existingOrder.getReference()
            );
        }
        
        sql = """
            UPDATE "order" 
            SET reference = ?, creation_datetime = ?, order_type = ?, order_status = ? 
            WHERE id = ? 
            RETURNING id
            """;
    }

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, orderToSave.getReference());
        ps.setTimestamp(2, Timestamp.from(orderToSave.getCreationDatetime()));
        ps.setString(3, orderToSave.getOrderType().name());
        ps.setString(4, orderToSave.getOrderStatus().name());
        
        if (!isNewOrder) {
            ps.setInt(5, orderToSave.getId());
        }

        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            orderToSave.setId(rs.getInt("id"));
        }
        
        conn.commit();
        
    } catch (SQLException e) {
        e.printStackTrace();
        throw new RuntimeException("Erreur lors de la sauvegarde de la commande", e);
    }

    return orderToSave;
}


private Order findOrderById(int id) {
    Order order = null;
    String sql = """
            SELECT id, reference, creation_datetime, order_type, order_status 
            FROM "order" 
            WHERE id = ?
            """;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            order = new Order();
            order.setId(rs.getInt("id"));
            order.setReference(rs.getString("reference"));
            order.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());
            
            String orderTypeStr = rs.getString("order_type");
            if (orderTypeStr != null) {
                order.setOrderType(OrderType.valueOf(orderTypeStr));
            }
            
            String orderStatusStr = rs.getString("order_status");
            if (orderStatusStr != null) {
                order.setOrderStatus(OrderStatus.valueOf(orderStatusStr));
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return order;
}


public Ingredient saveIngredient(Ingredient ingredientToSave) {
   
    throw new RuntimeException("Not implemented yet");
}

public Dish saveDish(Dish dishToSave) {
   
    throw new RuntimeException("Not implemented yet");
}