package org.wuerthner.ambitusdesktop;

import org.wuerthner.cwn.api.CwnAccent;
import org.wuerthner.cwn.api.CwnSymbolEvent;

public enum NoteSelector {
    N1, N2, N4, N8, N16, N32, N64, T1, T2, T3, T4, T5, T6, T7, V1, V2, L1, L2, L3,
    Accent(CwnAccent.indexOf(CwnAccent.ACCENT_ACCENT), 1),
    DownBow(CwnAccent.indexOf(CwnAccent.ACCENT_DOWNBOW), 1),
    DownMordent(CwnAccent.indexOf(CwnAccent.ACCENT_DOWNMORDENT), 1),
    DownPrall(CwnAccent.indexOf(CwnAccent.ACCENT_DOWNPRALL), 1),
    Espressivo(CwnAccent.indexOf(CwnAccent.ACCENT_ESPRESSIVO), 1),
    Fermata(CwnAccent.indexOf(CwnAccent.ACCENT_FERMATA), 1),
    Flageolet(CwnAccent.indexOf(CwnAccent.ACCENT_FLAGEOLET), 1),
    HalfOpen(CwnAccent.indexOf(CwnAccent.ACCENT_HALFOPEN), 1),
    LinePrall(CwnAccent.indexOf(CwnAccent.ACCENT_LINEPRALL), 1),
    LongFermata(CwnAccent.indexOf(CwnAccent.ACCENT_LONGFERMATA), 1),
    Marcato(CwnAccent.indexOf(CwnAccent.ACCENT_MARCATO), 1),
    Mordent(CwnAccent.indexOf(CwnAccent.ACCENT_MORDENT), 1),
    Open(CwnAccent.indexOf(CwnAccent.ACCENT_OPEN), 1),
    Portato(CwnAccent.indexOf(CwnAccent.ACCENT_PORTATO), 1),
    PrallDown(CwnAccent.indexOf(CwnAccent.ACCENT_PRALLDOWN), 1),
    PrallMordent(CwnAccent.indexOf(CwnAccent.ACCENT_PRALLMORDENT), 1),
    Prall(CwnAccent.indexOf(CwnAccent.ACCENT_PRALL), 1),
    PrallPrall(CwnAccent.indexOf(CwnAccent.ACCENT_PRALLPRALL), 1),
    PrallUp(CwnAccent.indexOf(CwnAccent.ACCENT_PRALLUP), 1),
    ReverseTurn(CwnAccent.indexOf(CwnAccent.ACCENT_REVERSETURN), 1),
    ShortFermata(CwnAccent.indexOf(CwnAccent.ACCENT_SHORTFERMATA), 1),
    Snappizzicato(CwnAccent.indexOf(CwnAccent.ACCENT_SNAPPIZZICATO), 1),
    Staccatissimo(CwnAccent.indexOf(CwnAccent.ACCENT_STACCATISSIMO), 1),
    Staccato(CwnAccent.indexOf(CwnAccent.ACCENT_STACCATO), 1),
    Stopped(CwnAccent.indexOf(CwnAccent.ACCENT_STOPPED), 1),
    Tenuto(CwnAccent.indexOf(CwnAccent.ACCENT_TENUTO), 1),
    Trill(CwnAccent.indexOf(CwnAccent.ACCENT_TRILL), 1),
    Turn(CwnAccent.indexOf(CwnAccent.ACCENT_TURN), 1),
    UpBow(CwnAccent.indexOf(CwnAccent.ACCENT_UPBOW), 1),
    UpMordent(CwnAccent.indexOf(CwnAccent.ACCENT_UPMORDENT), 1),
    UpPrall(CwnAccent.indexOf(CwnAccent.ACCENT_UPPRALL), 1),
    o15va(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_15VA), 2),
    o8va(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_8VA), 2),
    BowDown(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_BOWDOWN), 2),
    BowUp(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_BOWUP), 2),
    Case1(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_CASE1), 2),
    Case2(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_CASE2), 2),
    Crescendo(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_CRESCENDO), 2),
    Decrescendo(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_DECRESCENDO), 2),
    FFF(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_FFF), 2),
    FF(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_FF), 2),
    F(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_F), 2),
    FP(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_FP), 2),
    Label1(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_LABEL1), 2),
    Label2(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_LABEL2), 2),
    Label3(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_LABEL3), 2),
    MF(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_MF), 2),
    MP(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_MP), 2),
    Pedal1(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_PEDAL1), 2),
    Pedal2(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_PEDAL2), 2),
    P(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_P), 2),
    PP(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_PP), 2),
    PPP(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_PPP), 2),
    SFF(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_SF), 2),
    SF(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_SF), 2),
    SFZ(CwnSymbolEvent.indexOf(CwnSymbolEvent.SYMBOL_SFZ), 2)
    ;

    private final int type;
    private final int index;

    private NoteSelector() {
        index = 0;
        type = 0;
    }

    private NoteSelector(int i, int t) {
        index = i;
        type = t;
    }

    public boolean isNote() {
        return type==0;
    }

    public boolean isAccent() {
        return type==1;
    }

    public boolean isSymbol() {
        return type==2;
    }

    public int getIndex() { return index; }

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
    
    public static NoteSelector get(String name) {
        switch(name) {
            case "Accent":
                return NoteSelector.Accent;
            case "DownBow":
                return NoteSelector.DownBow;
            case "DownMordent":
                return NoteSelector.DownMordent;
            case "DownPrall":
                return NoteSelector.DownPrall;
            case "Espressivo":
                return NoteSelector.Espressivo;
            case "Fermata":
                return NoteSelector.Fermata;
            case "Flageolet":
                return NoteSelector.Flageolet;
            case "HalfOpen":
                return NoteSelector.HalfOpen;
            case "LinePrall":
                return NoteSelector.LinePrall;
            case "LongFermata":
                return NoteSelector.LongFermata;
            case "Marcato":
                return NoteSelector.Marcato;
            case "Mordent":
                return NoteSelector.Mordent;
            case "Open":
                return NoteSelector.Open;
            case "Portato":
                return NoteSelector.Portato;
            case "PrallDown":
                return NoteSelector.PrallDown;
            case "PrallMordent":
                return NoteSelector.PrallMordent;
            case "Prall":
                return NoteSelector.Prall;
            case "PrallPrall":
                return NoteSelector.PrallPrall;
            case "PrallUp":
                return NoteSelector.PrallUp;
            case "ReverseTurn":
                return NoteSelector.ReverseTurn;
            case "ShortFermata":
                return NoteSelector.ShortFermata;
            case "Snappizzicato":
                return NoteSelector.Snappizzicato;
            case "Staccatissimo":
                return NoteSelector.Staccatissimo;
            case "Staccato":
                return NoteSelector.Staccato;
            case "Stopped":
                return NoteSelector.Stopped;
            case "Tenuto":
                return NoteSelector.Tenuto;
            case "Trill":
                return NoteSelector.Trill;
            case "Turn":
                return NoteSelector.Turn;
            case "UpBow":
                return NoteSelector.UpBow;
            case "UpMordent":
                return NoteSelector.UpMordent;
            case "UpPrall":
                return NoteSelector.UpPrall;
            case "15va": return NoteSelector.o15va;
            case "8va": return NoteSelector.o8va;
            case "BowDown": return NoteSelector.BowDown;
            case "BowUp": return NoteSelector.BowUp;
            case "Case1": return NoteSelector.Case1;
            case "Case2": return NoteSelector.Case2;
            case "Crescendo": return NoteSelector.Crescendo;
            case "Decrescendo": return NoteSelector.Decrescendo;
            case "FFF": return NoteSelector.FFF;
            case "FF": return NoteSelector.FF;
            case "F": return NoteSelector.F;
            case "FP": return NoteSelector.FP;
            case "Label1": return NoteSelector.Label1;
            case "Label2": return NoteSelector.Label2;
            case "Label3": return NoteSelector.Label3;
            case "MF": return NoteSelector.MF;
            case "MP": return NoteSelector.MP;
            case "Pedal1": return NoteSelector.Pedal1;
            case "Pedal2": return NoteSelector.Pedal2;
            case "P": return NoteSelector.P;
            case "PP": return NoteSelector.PP;
            case "PPP": return NoteSelector.PPP;
            case "SFF": return NoteSelector.SFF;
            case "SF": return NoteSelector.SF;
            case "SFZ": return NoteSelector.SFZ;
            default:
                return null;
        }
    }
}
