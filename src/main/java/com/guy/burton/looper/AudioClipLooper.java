package com.guy.burton.looper;

import javax.sound.midi.Receiver;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;
import java.util.function.Consumer;

public class AudioClipLooper {

    private final SourceDataLine sourceDataLine;
    private final LooperController looperController;
    private final Consumer<Exception> errorHandler;
    private final Receiver receiver;
    private volatile boolean cancelled;
    private Thread audioThread;

    public AudioClipLooper(SourceDataLine sourceDataLine, Receiver receiver, Consumer<Exception> errorHandler) {
        this.sourceDataLine = sourceDataLine;
        this.errorHandler = errorHandler;
        this.receiver = receiver;
        looperController = new LooperController(receiver);
    }

    public synchronized void setupLoop(AudioInputStream audioInputStream)  {
        System.out.println("Playing audio file to " + sourceDataLine.getLineInfo());

        cancelled = false;
        audioThread = new Thread(() -> run(audioInputStream));
        audioThread.start();
    }

    private void run(AudioInputStream audioInputStream)
    {
        try {
            sourceDataLine.start();
            System.out.println("Starting loop");
            looperController.record();
            byte[] buffer = new byte[sourceDataLine.getBufferSize()];
            while (!cancelled) {
                int bytesRead = audioInputStream.read(buffer, 0, buffer.length);
                if (bytesRead <= 0)
                    break;
                sourceDataLine.write(buffer, 0, bytesRead);
                Thread.yield();
            }
            audioInputStream.close();
            if (!cancelled) {
                sourceDataLine.drain();
                System.out.println("Ending loop");
                looperController.play();
            }

        } catch (Exception e) {
            errorHandler.accept(e);
        }
    }

    public synchronized void cancel() {
        System.out.println("Cancelling AudioClipLooper");
        if (audioThread == null || cancelled)
            return;

        cancelled = true;

        tryStop();

        try {
            audioThread.join();
        } catch (InterruptedException ignored) {
        }

        audioThread = null;

        tryStop();
    }

    private void tryStop() {
        try {
            looperController.stop();
        } catch (Exception ignored) {
        }

        try {
            sourceDataLine.stop();
        } catch (Exception ignored) {
        }
    }

    public void close() {
        System.out.println("Closing AudioClipLooper");
        sourceDataLine.close();
        receiver.close();
    }
}
