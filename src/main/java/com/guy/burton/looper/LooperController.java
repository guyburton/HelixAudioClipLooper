package com.guy.burton.looper;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

public class LooperController {
    private final Receiver receiver;

    public LooperController(Receiver receiver) {
        this.receiver = receiver;
    }

    public void openLooper() throws InvalidMidiDataException {
        receiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 67, 64), 0);
    }

    public void record() throws InvalidMidiDataException {
        receiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 60, 64), 0);
    }

    public void overdub() throws InvalidMidiDataException {
        receiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 60, 60), 0);
    }

    public void play() throws InvalidMidiDataException {
        receiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 61, 64), 0);
    }
    public void stop() throws InvalidMidiDataException {
        receiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 61, 0), 0);
    }

    public void closeLooper() throws InvalidMidiDataException {
        receiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 67, 0), 0);
    }

    
}
