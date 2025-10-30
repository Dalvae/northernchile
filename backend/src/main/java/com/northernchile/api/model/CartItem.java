
package com.northernchile.api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private TourSchedule schedule;

    @Column(nullable = false)
    private Integer numParticipants = 1;

    @CreationTimestamp
    private Instant createdAt;

    public CartItem() {
    }

    public CartItem(UUID id, Cart cart, TourSchedule schedule, Integer numParticipants, Instant createdAt) {
        this.id = id;
        this.cart = cart;
        this.schedule = schedule;
        this.numParticipants = numParticipants;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public TourSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(TourSchedule schedule) {
        this.schedule = schedule;
    }

    public Integer getNumParticipants() {
        return numParticipants;
    }

    public void setNumParticipants(Integer numParticipants) {
        this.numParticipants = numParticipants;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(id, cartItem.id) && Objects.equals(cart, cartItem.cart) && Objects.equals(schedule, cartItem.schedule) && Objects.equals(numParticipants, cartItem.numParticipants) && Objects.equals(createdAt, cartItem.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cart, schedule, numParticipants, createdAt);
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", cart=" + cart +
                ", schedule=" + schedule +
                ", numParticipants=" + numParticipants +
                ", createdAt=" + createdAt +
                '}';
    }
}
