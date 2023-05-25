package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :uid AND b.end < now() ORDER BY b.start DESC")
    List<Booking> findAllPastByBooker(@Param("uid") long uid);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :uid AND now() BETWEEN b.start AND b.end ORDER BY b.start DESC")
    List<Booking> findAllCurrentByBooker(@Param("uid") long uid);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :uid AND b.start > now() ORDER BY b.start DESC")
    List<Booking> findAllFutureByBooker(@Param("uid") long uid);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :uid AND b.start > now() AND b.status = 'WAITING' ORDER BY b.start DESC")
    List<Booking> findAllWaitingByBooker(@Param("uid") long uid);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :uid AND b.status IN ('CANCELED', 'REJECTED') ORDER BY b.start DESC")
    List<Booking> findAllRejectedByBooker(@Param("uid") long uid);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :uid ORDER BY b.start DESC")
    List<Booking> findAllByBooker(@Param("uid") long uid);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :uid AND b.end < now() ORDER BY b.start DESC")
    List<Booking> findAllPastByOwner(@Param("uid") long uid);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :uid AND now() BETWEEN b.start AND b.end ORDER BY b.start DESC")
    List<Booking> findAllCurrentByOwner(@Param("uid") long uid);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :uid AND b.start > now() ORDER BY b.start DESC")
    List<Booking> findAllFutureByOwner(@Param("uid") long uid);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :uid AND b.start > now() AND b.status = 'WAITING' ORDER BY b.start DESC")
    List<Booking> findAllWaitingByOwner(@Param("uid") long uid);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :uid AND b.status IN ('CANCELED', 'REJECTED') ORDER BY b.start DESC")
    List<Booking> findAllRejectedByOwner(@Param("uid") long uid);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :uid ORDER BY b.start DESC")
    List<Booking> findAllByOwner(@Param("uid") long uid);

    List<Booking> findAllByItemOwnerIdOrderByStartDesc(long userId);

    List<Booking> findAllByItemIdIn(Collection<Long> itemIds);

    @Query("SELECT b FROM Booking b WHERE item.id=?1 AND b.status in ('APPROVED', 'WAITING') ORDER BY b.start ASC")
    List<Booking> findAllByItemId(long itemId);
}
