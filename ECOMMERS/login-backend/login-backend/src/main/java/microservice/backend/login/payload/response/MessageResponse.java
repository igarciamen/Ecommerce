package microservice.backend.login.payload.response;

public class MessageResponse {
    private String message;

    public MessageResponse() {
        System.out.println("🔄 [MessageResponse] constructor vacío");
    }

    public MessageResponse(String message) {
        this.message = message;
        System.out.println("🔄 [MessageResponse] constructor con message -> '" + message + "'");
    }

    public String getMessage() {
        System.out.println("📥 [MessageResponse] getMessage -> " + message);
        return message;
    }

    public void setMessage(String message) {
        System.out.println("📤 [MessageResponse] setMessage -> " + message);
        this.message = message;
    }
}
