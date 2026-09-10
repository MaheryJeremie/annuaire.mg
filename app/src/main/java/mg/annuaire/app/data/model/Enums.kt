package mg.annuaire.app.data.model

enum class UserRole {
    PROVIDER,
    AGENT
}

enum class CertificationStatus {
    NONE,
    PENDING,
    CERTIFIED,
    REJECTED
}

enum class MetierStatus {
    APPROVED,
    PENDING,
    REJECTED
}

enum class NetworkMode {
    WIFI_ONLY,
    CELLULAR_ONLY,
    ANY
}
