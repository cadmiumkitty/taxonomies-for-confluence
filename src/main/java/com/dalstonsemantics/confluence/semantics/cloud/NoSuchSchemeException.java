package com.dalstonsemantics.confluence.semantics.cloud;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Invalid scheme URI.")
public class NoSuchSchemeException extends RuntimeException {
}
