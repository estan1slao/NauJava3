package ru.stanislav.NauJava3.entity;

import jakarta.persistence.*;
import ru.stanislav.NauJava3.entity.enums.TransactionType;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(length = 255)
    private String description;

    @Column(length = 7)
    private String color;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected Category() {
    }

    public Category(AppUser user, String name, TransactionType type,
                    String description, String color) {
        this.user = user;
        this.name = name;
        this.type = type;
        this.description = description;
        this.color = color;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public TransactionType getType() {
        return type;
    }
}
