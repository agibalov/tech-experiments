package io.agibalov;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DataConfiguration {
    private int numberOfAccounts;
    private int numberOfUsersPerAccount;
}
