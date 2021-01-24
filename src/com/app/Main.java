package com.app;

import math_utils.Rational;
import parser.Parser;

import java.util.Random;

public class Main {

    public static void main(String[] args) {
        Random r = new Random();
        Parser p = new Parser("[1..100] {+|-|x|:|^} [2..10]");
        System.out.println(p.createEquation(r));
    }
}
