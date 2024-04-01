package matcher;

import com.github.karixdev.courseservice.domain.entity.Course;
import lombok.RequiredArgsConstructor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.util.Objects;

@RequiredArgsConstructor
public class CourseNonIdArgumentMatcher implements ArgumentMatcher<Course> {

    private final Course course;

    public static Course courseNonIdEq(Course course) {
        return Mockito.argThat(new CourseNonIdArgumentMatcher(course));
    }

    @Override
    public boolean matches(Course other) {
        return Objects.equals(other.getScheduleId(), course.getScheduleId())
                && Objects.equals(other.getName(), course.getName())
                && other.getCourseType() == course.getCourseType()
                && Objects.equals(other.getTeachers(), course.getTeachers())
                && Objects.equals(other.getClassrooms(), course.getClassrooms())
                && Objects.equals(other.getAdditionalInfo(), course.getAdditionalInfo())
                && other.getDayOfWeek() == course.getDayOfWeek()
                && other.getWeekType() == course.getWeekType()
                && Objects.equals(other.getStartsAt(), course.getStartsAt())
                && Objects.equals(other.getEndsAt(), course.getEndsAt());
    }
}
