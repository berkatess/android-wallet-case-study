package com.ba.walletcase.domain.demo

/**
 * Demo-only: which data scenario the mock data source should serve, so a reviewer
 * can flip the four UI states live. Delete together with the rest of the demo seam
 * when a real data source replaces the mock.
 */
enum class DataScenario {
    LOADED,
    EMPTY,
    ERROR,
}
