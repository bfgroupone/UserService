package groupone.userservice.entity;


import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Entity
@Table(name="User")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId", unique = true)
    private Integer id;

    @Column(nullable = false, unique = true)
    @NotEmpty(message = "Please provide your email")
    private String email;

    @Column(nullable = false)
    @NotEmpty(message = "Please provide your first name")
    private String firstName;

    @Column(nullable = false)
    @NotEmpty(message = "Please provide your last name")
    private String lastName;

    @Column(nullable = false)
    @NotEmpty(message = "Please provide your password")
    private String password;

    @Column(nullable = false)
    private Date dateJoined;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private int type;

    @Column
    private String profileImageURL;

    @Column
    private String validationToken;
}