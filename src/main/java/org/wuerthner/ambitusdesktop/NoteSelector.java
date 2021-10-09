package org.wuerthner.ambitusdesktop;

public enum NoteSelector {
    N1, N2, N4, N8, N16, N32, N64, T1, T2, T3, T4, T5, T6, T7;

    public float getNoteLength() { return getNoteLength(this); }

    public int getNoteIndex() { return getNoteIndex(this); }

    public float getTupletFactor() { return getTupletFactor(this); }

    public static float getNoteLength(NoteSelector selector) {
        switch (selector) {
            case N1: return 1.0f;
            case N2: return 0.5f;
            case N4: return 0.25f;
            case N8: return 0.125f;
            case N16: return 0.0625f;
            case N32: return 0.03125f;
            case N64: return 0.015625f;
            default: return 1.0f;
        }
    }

    public static int getNoteIndex(NoteSelector selector) {
        switch (selector) {
            case N1: return 0;
            case N2: return 1;
            case N4: return 2;
            case N8: return 3;
            case N16: return 4;
            case N32: return 5;
            case N64: return 6;
            default: return 3;
        }
    }

    public static float getTupletFactor(NoteSelector selector) {
        switch (selector) {
            case T1: return 1.0f;
            case T2: return 3.0f/2.0f;
            case T3: return 2.0f/3.0f;
            case T4: return 3.0f/4.0f;
            case T5: return 4.0f/5.0f;
            case T6: return 5.0f/6.0f;
            case T7: return 6.0f/7.0f;
            default: return 1.0f;
        }
    }

    public static float getNoteLength(int ppq, NoteSelector selector) {
        return ppq*4*getNoteLength(selector);
    }
}
