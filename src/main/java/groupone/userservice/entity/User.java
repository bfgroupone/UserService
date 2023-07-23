package groupone.userservice.entity;


import lombok.*;

import javax.persistence.*;
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
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
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