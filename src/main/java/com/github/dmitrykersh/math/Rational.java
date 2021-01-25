package com.github.dmitrykersh.math;

import org.jetbrains.annotations.NotNull;

public class Rational {
    private int numerator_;
    private int denominator_;

    public Rational(int value){
        numerator_ = value;
        denominator_ = 1;
    }

    public Rational(int numerator_, int denominator_){
        this.denominator_ = denominator_ == 0 ? 1 : denominator_;
        this.numerator_ = numerator_;

        reduce();
    }

    @Override
    public String toString(){
        return numerator_ + (denominator_ != 1 ? "/" + denominator_ : "");
    }


    public void printAsDouble(int precision){
        System.out.format("%." + precision + "f", getAsDouble());
    }

    public double getAsDouble(){
        return (double)numerator_ / denominator_;
    }

    public void add(final @NotNull Rational other){
        if (denominator_ == other.denominator_){
            numerator_ += other.numerator_;
        } else {
            // a/b.add(c/d) = ad + /bd
            denominator_ *= other.denominator_;
            numerator_ *= other.denominator_;
            numerator_ += denominator_ * other.numerator_;
        }
        reduce();
    }

    public void subtract(final @NotNull Rational other){
        if (denominator_ == other.denominator_){
            numerator_ -= other.numerator_;
        } else {
            // a/b.add(c/d) = ad + /bd
            denominator_ *= other.denominator_;
            numerator_ *= other.denominator_;
            numerator_ -= denominator_ * other.numerator_;
        }
        reduce();
    }

    public void multiply(final @NotNull Rational other){
        numerator_ *= other.numerator_;
        denominator_ *= other.denominator_;
        reduce();
    }

    public void divide(final @NotNull Rational other){
        numerator_ *= other.denominator_;
        denominator_ *= other.numerator_;
        reduce();
    }

    private static int gcd(int a, int b){
        if (a == 0 || b == 0)
            return Math.max(Math.max(a, b), 1);
        return gcd(b, a % b);
    }

    private void reduce(){
        int divisor = gcd(getNumerator_(), getDenominator_());

        setNumerator_(getNumerator_() / divisor);
        setDenominator_(getDenominator_() / divisor);

        if (getDenominator_() < 0){
            setNumerator_(-getNumerator_());
            setDenominator_(-getDenominator_());
        }
    }

    public int getNumerator_() {
        return numerator_;
    }

    public void setNumerator_(int numerator_) {
        this.numerator_ = numerator_;
    }

    public int getDenominator_() {
        return denominator_;
    }

    public void setDenominator_(int denominator_) {
        if (denominator_ != 0)
            this.denominator_ = denominator_;
    }
}
