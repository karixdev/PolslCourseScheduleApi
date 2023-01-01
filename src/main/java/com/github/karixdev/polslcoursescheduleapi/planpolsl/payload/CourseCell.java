package com.github.karixdev.polslcoursescheduleapi.planpolsl.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseCell {
    private int top;
    private int left;
    private int ch;
    private int cw;
    private String text;
}
