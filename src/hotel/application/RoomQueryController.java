package hotel.application;

import hotel.domain.Room;
import hotel.domain.RoomRepository;
import java.math.BigDecimal;
import java.util.List;

// Другий use-case: показати номери, щоб CLI не тримав їх список у собі.
public class RoomQueryController {

    // DTO для списку номерів
    public record RoomInfo(int number, String category, BigDecimal pricePerNight) {
        static RoomInfo from(Room room) {
            return new RoomInfo(room.number(), room.categoryLabel(), room.pricePerNight());
        }
    }

    private final RoomRepository rooms;

    public RoomQueryController(RoomRepository rooms) {
        this.rooms = rooms;
    }

    public List<RoomInfo> availableRooms() {
        return rooms.findAll().stream().map(RoomInfo::from).toList();
    }
}
