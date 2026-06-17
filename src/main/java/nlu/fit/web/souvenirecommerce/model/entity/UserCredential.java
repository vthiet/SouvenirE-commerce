package nlu.fit.web.souvenirecommerce.model.entity;

import jakarta.persistence.*;
import lombok.*;
import nlu.fit.web.souvenirecommerce.common.base.AbsBaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_credentials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE user_credentials SET deleted_at = CURRENT_TIMESTAMP WHERE user_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class UserCredential extends AbsBaseEntity {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "reset_code", length = 64)
    private String resetCode;

    @Column(name = "reset_expires_at")
    private LocalDateTime resetExpiresAt;
}
