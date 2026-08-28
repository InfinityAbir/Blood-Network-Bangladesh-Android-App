package com.bloodnetwork.bangladesh.data.model

/** String enums matching the API's JsonStringEnumConverter (enums serialized as PascalCase strings). */
enum class BloodGroup(val label: String) {
    APositive("A+"),
    ANegative("A-"),
    BPositive("B+"),
    BNegative("B-"),
    ABPositive("AB+"),
    ABNegative("AB-"),
    OPositive("O+"),
    ONegative("O-");

    companion object {
        val all: List<BloodGroup> = entries
    }
}

enum class Urgency {
    Critical, Urgent, Normal
}

enum class AvailabilityStatus {
    Available, Unavailable, RecentlyDonated, Unknown
}

enum class RequestStatus {
    Open, PartiallyFulfilled, Fulfilled, Cancelled, Expired
}

enum class UserRole {
    Admin, Donor, Requester
}

enum class VerificationStatus {
    Unverified, Pending, Verified, Rejected
}

enum class NotificationType {
    BloodRequest, Match, System, Report
}

enum class DonorResponse {
    Pending, Accepted, Declined, Contacted
}
