package com.dalstonsemantics.confluence.semantics.cloud;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.UNAUTHORIZED, reason="Unauthorized to upload taxonomy.")
public class NotAuthorizedToUploadTaxonomyException extends RuntimeException {
}
