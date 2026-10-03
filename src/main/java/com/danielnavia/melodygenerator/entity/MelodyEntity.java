package com.danielnavia.melodygenerator.entity;

import com.danielnavia.melodygenerator.model.Measure;
import com.danielnavia.melodygenerator.model.Mode;
import com.danielnavia.melodygenerator.model.NoteName;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "melodies")
public class MelodyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)//especifica el tip0o de enum
    @Column(name = "root_note", nullable = false)
    private NoteName rootNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false)
    private Mode mode;

    @Column(name = "name", nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "melody", columnDefinition = "jsonb")
    private List<Measure> measures;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public MelodyEntity(UserEntity user, NoteName rootNote, Mode mode,
                        List<Measure> measures, LocalDateTime createdAt) {
        this.user = user;
        this.rootNote = rootNote;
        this.mode = mode;
        this.measures = measures;
        this.createdAt = createdAt;
    }
}
