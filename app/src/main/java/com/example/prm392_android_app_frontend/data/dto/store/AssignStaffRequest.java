package com.example.prm392_android_app_frontend.data.dto.store;

public class AssignStaffRequest {
    public Integer staffId;
    public Integer actorUserId;

    public AssignStaffRequest(Integer staffId, Integer actorUserId) {
        this.staffId = staffId;
        this.actorUserId = actorUserId;
    }
}
