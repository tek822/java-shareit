package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b WHERE booker.id=?1 ORDER BY b.start DESC")
    List<Booking> findAllByBooker(long userId);

    List<Booking> findAllByItemOwnerIdOrderByStartDesc(long userId);

    @Query("SELECT b FROM Booking b WHERE item.id=?1 AND b.status in ('APPROVED', 'WAITING') ORDER BY b.start ASC")
    List<Booking> findAllByItemId(long itemId);
}
