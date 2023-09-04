package com.dalstonsemantics.confluence.semantics.cloud;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.FORBIDDEN, reason="Forbidden to perform this SPARQL query.")
public class SparqlForbiddenException extends RuntimeException {
}
