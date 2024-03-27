package com.github.karixdev.scheduleservice.application.command;

import java.util.List;
import java.util.UUID;

public record BlankSchedulesUpdateCommand(List<UUID> ids) {}
