package com.birthday.guardian.common;

public enum MembershipLevel {
    FREE(3, "温馨体验"),
    VIP(20, "挚爱守护");

    private final int maxRoleCount;
    private final String friendlyName;

    MembershipLevel(int maxRoleCount, String friendlyName) {
        this.maxRoleCount = maxRoleCount;
        this.friendlyName = friendlyName;
    }

    public int getMaxRoleCount() {
        return maxRoleCount;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public static MembershipLevel fromCode(String code) {
        if (code == null) {
            return FREE;
        }
        try {
            return MembershipLevel.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return FREE;
        }
    }

    public boolean isVip() {
        return this == VIP;
    }
}
