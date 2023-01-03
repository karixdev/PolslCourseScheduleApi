package com.github.karixdev.polslcoursescheduleapi.discord;

import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import lombok.*;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "discord_web_hook",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = "url",
                        name = "discord_web_hook_url_unique"
                )
        }
)
public class DiscordWebHook {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "discord_web_hook_gen"
    )
    @SequenceGenerator(
            name = "discord_web_hook_gen",
            sequenceName = "discord_web_hook_seq",
            allocationSize = 1
    )
    @Column(
            name = "id",
            nullable = false,
            updatable = false
    )
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(
            name = "url",
            nullable = false
    )
    private String url;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(
            name = "added_by_id",
            nullable = false
    )
    private User addedBy;

    @ToString.Exclude
    @ManyToMany
    @JoinTable(
            name = "discord_web_hook_schedules",
            joinColumns = @JoinColumn(
                    name = "discord_web_hook_id",
                    referencedColumnName = "id",
                    foreignKey = @ForeignKey(
                            name = "discord_web_hook_schedules_web_hook_id_fk"
                    )
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "schedules_id",
                    referencedColumnName = "id",
                    foreignKey = @ForeignKey(
                            name = "discord_web_hook_schedules_schedule_id_fk"
                    )
            )
    )
    @Builder.Default
    private Set<Schedule> schedules = new LinkedHashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscordWebHook that = (DiscordWebHook) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(url, that.url) &&
                Objects.equals(addedBy.getId(), that.addedBy.getId()) &&
                Objects.equals(schedules, that.schedules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, addedBy.getId(), schedules);
    }
}
