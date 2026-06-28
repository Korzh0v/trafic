package com.example.demo;

class PassengerStats {
    int total = 0;
    int count = 0;
    int peak = 0;

    void addSample(int p) {
        total += p;
        count++;
        if (p > peak) peak = p;
    }

    double average() {
        return count == 0 ? 0 : (double) total / count;
    }
}