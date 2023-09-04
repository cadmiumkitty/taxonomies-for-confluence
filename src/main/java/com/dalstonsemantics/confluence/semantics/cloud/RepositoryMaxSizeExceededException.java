package com.dalstonsemantics.confluence.semantics.cloud;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.FORBIDDEN, reason="Repository max size exceeded.")
public class RepositoryMaxSizeExceededException extends RuntimeException {
}
