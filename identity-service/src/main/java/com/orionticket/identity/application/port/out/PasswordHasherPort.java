package com.orionticket.identity.application.port.out;

public interface PasswordHasherPort {
    String hash(String rawPassword);
}
