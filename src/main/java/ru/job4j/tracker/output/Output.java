package ru.job4j.tracker.output;

import java.io.OutputStream;
import java.io.PrintStream;

public class Output extends PrintStream {
    public Output(OutputStream out) {
        super(out);
    }
}
