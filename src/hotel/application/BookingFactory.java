package hotel.application;

import hotel.domain.AdditionalService;
import hotel.domain.Booking;
import hotel.domain.Guest;
import hotel.domain.RoomRepository;
import hotel.domain.SeasonPolicy;
import hotel.domain.StayPricing;
import hotel.domain.TouristTaxPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

// Creator: єдине місце, де із запиту збирається валідне бронювання.
public class BookingFactory {
    private final RoomRepository rooms;
    private final StayPricingAssembler assembler;
    private final AtomicLong guestIds = new AtomicLong(9);
    private final AtomicLong bookingIds = new AtomicLong(500);

    public BookingFactory(RoomRepository rooms, TouristTaxPolicy taxPolicy) {
        this.rooms = rooms;
        this.assembler = new StayPricingAssembler(taxPolicy);
    }

    public Booking create(BookingRequest req) {
        var guest = Guest.register(guestIds.incrementAndGet(), req.guestName(), req.phone(), req.passportNumber());
        var room = rooms.getByNumber(req.roomNumber());
        var pricing = assembler.assemble(req.nights(), req.season(), req.breakfast(), req.parking());
        return Booking.create(bookingIds.incrementAndGet(), guest, room, pricing);
    }
}

// Pure Fabrication: переводить прапорці з запиту (сезон, сніданок, паркування) на доменні об'єкти.
// Це єдине місце, де знають, які послуги є в готелі.
class StayPricingAssembler {
    private final TouristTaxPolicy taxPolicy;

    StayPricingAssembler(TouristTaxPolicy taxPolicy) {
        this.taxPolicy = taxPolicy;
    }

    StayPricing assemble(int nights, String seasonName, boolean breakfast, boolean parking) {
        List<AdditionalService> services = new ArrayList<>();
        if (breakfast) {
            services.add(AdditionalService.breakfast());
        }
        if (parking) {
            services.add(AdditionalService.parking());
        }
        return StayPricing.of(nights, SeasonPolicy.byName(seasonName), services, taxPolicy);
    }
}
