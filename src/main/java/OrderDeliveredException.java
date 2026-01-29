public class OrderDeliveredException extends RuntimeException {
    public OrderDeliveredException(String message) {
        super(message);
    }
    
    public OrderDeliveredException(String message, Throwable cause) {
        super(message, cause);
    }
}