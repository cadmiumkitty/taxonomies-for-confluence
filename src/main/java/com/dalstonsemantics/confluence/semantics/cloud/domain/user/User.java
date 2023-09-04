package com.dalstonsemantics.confluence.semantics.cloud.domain.user;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder
public class User {
    String accountId;
    String accountType;
    String publicName;
    @Singular
    List<Operation> operations;
    ProfilePicture profilePicture;
}
