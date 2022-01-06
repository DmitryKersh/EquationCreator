package com.github.dmitrykersh.equationcreator.api.math;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

// TODO: use existing Java library
@Getter
@Setter
public class Rational {
    private int numerator;
    private int denominator;

    public Rational(int value) {
        numerator = value;
        denominator = 1;
    }

    public Rational(int numerator, int denominator) {
        this.denominator = denominator == 0 ? 1 : denominator;
        this.numerator = numerator;

        reduce();
    }

    @Override
    public String toString() {
        return numerator + (denominator != 1 ? "/" + denominator : "");
    }


    public void printAsDouble(int precision) {
        System.out.format("%." + precision + "f", getAsDouble());
    }

    public double getAsDouble() {
        return (double) numerator / denominator;
    }

    public void add(final @NotNull Rational other) {
        if (denominator == other.denominator) {
            numerator += other.numerator;
        } else {
            // a/b.add(c/d) = ad + /bd
            denominator *= other.denominator;
            numerator *= other.denominator;
            numerator += denominator * other.numerator;
        }
        reduce();
    }

    public void subtract(final @NotNull Rational other) {
        if (denominator == other.denominator) {
            numerator -= other.numerator;
        } else {
            // a/b.add(c/d) = ad + /bd
            denominator *= other.denominator;
            numerator *= other.denominator;
            numerator -= denominator * other.numerator;
        }
        reduce();
    }

    public void multiply(final @NotNull Rational other) {
        numerator *= other.numerator;
        denominator *= other.denominator;
        reduce();
    }

    public void divide(final @NotNull Rational other) {
        numerator *= other.denominator;
        denominator *= other.numerator;
        reduce();
    }

    private static int gcd(int a, int b) {
        return a == 0 || b == 0 ? Math.max(Math.max(a, b), 1) : gcd(b, a % b);
    }

    private void reduce() {
        int divisor = gcd(getNumerator(), getDenominator());

        setNumerator(getNumerator() / divisor);
        setDenominator(getDenominator() / divisor);

        if (getDenominator() < 0) {
            setNumerator(-getNumerator());
            setDenominator(-getDenominator());
        }
    }

    public void setDenominator(int denominator) {
        if (denominator != 0) this.denominator = denominator;
    }
}
