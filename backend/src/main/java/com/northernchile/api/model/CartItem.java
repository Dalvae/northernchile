
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
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(nullable = false)
    private LocalDate tourDate;

    @Column(nullable = false)
    private Integer numAdults;

    private Integer numChildren = 0;

    @CreationTimestamp
    private Instant createdAt;

    public CartItem() {
    }

    public CartItem(UUID id, Cart cart, Tour tour, LocalDate tourDate, Integer numAdults, Integer numChildren, Instant createdAt) {
        this.id = id;
        this.cart = cart;
        this.tour = tour;
        this.tourDate = tourDate;
        this.numAdults = numAdults;
        this.numChildren = numChildren;
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

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public LocalDate getTourDate() {
        return tourDate;
    }

    public void setTourDate(LocalDate tourDate) {
        this.tourDate = tourDate;
    }

    public Integer getNumAdults() {
        return numAdults;
    }

    public void setNumAdults(Integer numAdults) {
        this.numAdults = numAdults;
    }

    public Integer getNumChildren() {
        return numChildren;
    }

    public void setNumChildren(Integer numChildren) {
        this.numChildren = numChildren;
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
        return Objects.equals(id, cartItem.id) && Objects.equals(cart, cartItem.cart) && Objects.equals(tour, cartItem.tour) && Objects.equals(tourDate, cartItem.tourDate) && Objects.equals(numAdults, cartItem.numAdults) && Objects.equals(numChildren, cartItem.numChildren) && Objects.equals(createdAt, cartItem.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cart, tour, tourDate, numAdults, numChildren, createdAt);
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", cart=" + cart +
                ", tour=" + tour +
                ", tourDate=" + tourDate +
                ", numAdults=" + numAdults +
                ", numChildren=" + numChildren +
                ", createdAt=" + createdAt +
                '}';
    }
}
