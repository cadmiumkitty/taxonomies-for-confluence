package com.dalstonsemantics.confluence.semantics.cloud;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Unauthorized to perform this SPARQL expression.")
public class SparqlMalformedQueryException extends RuntimeException {
}
