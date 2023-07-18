package groupone.userservice.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="history")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "historyId", unique = true)
    private Integer id;

    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private Integer postId;

    @Column(nullable = false)
    private Date viewDate;
}
