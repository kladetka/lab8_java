package hotel.application;

import java.math.BigDecimal;

// Indirection + Protected Variations: сьогодні SMS, завтра може бути email чи месенджер
public interface NotificationSender {
    void sendBookingConfirmation(String phone, long bookingId, BigDecimal total);
}
