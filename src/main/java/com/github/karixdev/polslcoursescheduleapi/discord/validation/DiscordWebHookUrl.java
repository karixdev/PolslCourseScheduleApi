package com.github.karixdev.polslcoursescheduleapi.discord.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DiscordWebHookUrlValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DiscordWebHookUrl {
    String message() default "Invalid discord web hook url";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
