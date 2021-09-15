package org.wuerthner.ambitusdesktop;

public enum NoteSelector {
    N1, N2, N4, N8, N16, N32, N64;

    public float getNoteLength() {
        return getNoteLength(this);
    }

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


    public static float getNoteLength(int ppq, NoteSelector selector) {
        return ppq*4*getNoteLength(selector);
    }
}
