package karpiuk.test.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@SQLDelete(sql = "UPDATE users SET is_deleted=TRUE WHERE id=?")
@SQLRestriction(value = "is_deleted=false")
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    private RoleName roleName;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    public Role(RoleName roleName) {
        this.roleName = roleName;
    }

    public Role() {
    }

    public enum RoleName {
        ROLE_USER,
        ROLE_ADMIN
    }
}
