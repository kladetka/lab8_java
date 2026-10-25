package hotel.domain;

import java.util.List;

public interface RoomRepository {
    /** Кидає IllegalArgumentException, якщо такого номера немає. */
    Room getByNumber(int roomNumber);

    List<Room> findAll();
}
