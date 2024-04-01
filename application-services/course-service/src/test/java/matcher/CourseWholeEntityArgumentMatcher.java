package matcher;

import com.github.karixdev.courseservice.domain.entity.Course;
import lombok.RequiredArgsConstructor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.util.Objects;

@RequiredArgsConstructor
public class CourseWholeEntityArgumentMatcher implements ArgumentMatcher<Course> {

    private final Course course;

    public static Course courseWholeEntityEq(Course course) {
        return Mockito.argThat(new CourseNonIdArgumentMatcher(course));
    }


    @Override
    public boolean matches(Course other) {
        return new CourseNonIdArgumentMatcher(course).matches(other)
                && Objects.equals(other.getId(), course.getId());
    }
}
