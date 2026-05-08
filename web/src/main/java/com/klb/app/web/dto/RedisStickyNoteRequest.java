package com.klb.app.web.dto;

import jakarta.validation.constraints.Size;

/** Body POST luu ghi chu demo vao Redis. */
public record RedisStickyNoteRequest(@Size(max = 2000) String text) {
}
