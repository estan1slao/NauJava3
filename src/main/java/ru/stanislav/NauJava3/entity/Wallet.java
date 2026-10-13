package ru.stanislav.NauJava3.entity;

import jakarta.persistence.*;
import ru.stanislav.NauJava3.entity.enums.WalletType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "wallet_type", nullable = false)
    private WalletType walletType;

    @Column(length = 255)
    private String description;

    @Column(name = "opening_balance", nullable = false,
            precision = 15, scale = 2)
    private BigDecimal openingBalance;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Wallet() {
    }

    public Wallet(AppUser user, String name, String currency,
                  WalletType walletType, String description,
                  BigDecimal openingBalance) {
        this.user = user;
        this.name = name;
        this.currency = currency;
        this.walletType = walletType;
        this.description = description;
        this.openingBalance = openingBalance;
        this.createdAt = LocalDateTime.now();
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
}
