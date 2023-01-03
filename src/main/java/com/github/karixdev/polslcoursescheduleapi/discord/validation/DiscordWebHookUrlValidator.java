package com.github.karixdev.polslcoursescheduleapi.discord.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DiscordWebHookUrlValidator implements
        ConstraintValidator<DiscordWebHookUrl, String> {
    @Override
    public boolean isValid(String url, ConstraintValidatorContext constraintValidatorContext) {
        return url.startsWith("https://discord.com/api/webhooks/");
    }
}
