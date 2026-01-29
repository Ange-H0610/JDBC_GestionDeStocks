
public class Main {
    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();
        
        // Test 1: Créer une nouvelle commande
        System.out.println("=== Test 1: Création d'une nouvelle commande ===");
        Order newOrder = new Order();
        newOrder.setReference("CMD-2026-001");
        newOrder.setOrderType(OrderType.EAT_IN);
        newOrder.setOrderStatus(OrderStatus.CREATED);
        
        Order savedOrder = dataRetriever.saveOrder(newOrder);
        System.out.println("Commande créée: " + savedOrder);
        
        // Test 2: Récupérer la commande
        System.out.println("\n=== Test 2: Récupération de la commande ===");
        Order retrievedOrder = dataRetriever.findOrderByReference("CMD-2026-001");
        System.out.println("Commande récupérée: " + retrievedOrder);
        
        // Test 3: Modifier la commande
        System.out.println("\n=== Test 3: Modification de la commande ===");
        retrievedOrder.setOrderStatus(OrderStatus.READY);
        Order updatedOrder = dataRetriever.saveOrder(retrievedOrder);
        System.out.println("Commande mise à jour: " + updatedOrder);
        
        // Test 4: Essayer de modifier une commande livrée
        System.out.println("\n=== Test 4: Test de la validation commande livrée ===");
        updatedOrder.setOrderStatus(OrderStatus.DELIVERED);
        dataRetriever.saveOrder(updatedOrder);
        
        // Essayer de modifier la date d'une commande livrée
        updatedOrder.setCreationDatetime(java.time.Instant.now().plusSeconds(3600));
        try {
            dataRetriever.saveOrder(updatedOrder);
            System.out.println("ERREUR: L'exception aurait dû être levée!");
        } catch (OrderDeliveredException e) {
            System.out.println("SUCCÈS: Exception correctement levée: " + e.getMessage());
        }
        
        // Tests existants
        Dish saladeVerte = dataRetriever.findDishById(1);
        System.out.println("\nPlat récupéré: " + saladeVerte);
    }
}