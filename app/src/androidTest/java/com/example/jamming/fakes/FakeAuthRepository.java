package com.example.jamming.fakes;

import com.example.jamming.repository.AuthRepository;

/**
 * Generic fake AuthRepository for UI tests across multiple screens.
 * Use uid=null to simulate "not logged in".
 */
public class FakeAuthRepository extends AuthRepository {

    private String uid;

    public FakeAuthRepository(String uid) {
        this.uid = uid;
    }

    /** Optional setter so a test can change auth state mid-test if needed. */
    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String getCurrentUid() {
        return uid;
    }
}
