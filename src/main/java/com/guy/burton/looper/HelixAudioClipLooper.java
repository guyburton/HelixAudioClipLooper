package com.guy.burton.looper;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class HelixAudioClipLooper {

    private final Consumer<Exception> errorHandler;
    private AudioClipLooper audioClipLooper;

    public HelixAudioClipLooper(Consumer<Exception> errorHandler) {
        this.errorHandler = errorHandler;
    }

    public static void main(String[] args) {

        JPanel topForm = new JPanel(new GridLayout(2,2));
        List<MidiDevice.Info> allMidiInterfaces = getMidiInterfaces();
        JComboBox<Object> midiInterfaces = new JComboBox<>(allMidiInterfaces.toArray());
        topForm.add(midiInterfaces);
        List<Mixer.Info> allAudioInterfaces = getAudioInterfaces();
        JComboBox<Object> audioInterfaces = new JComboBox<>(allAudioInterfaces.toArray());

        allAudioInterfaces.stream().filter(e -> e.getName().toUpperCase().startsWith("HELIX")).findAny().ifPresent(audioInterfaces::setSelectedItem);
        allMidiInterfaces.stream().filter(e -> e.getName().toUpperCase().startsWith("HELIX")).findAny().ifPresent(midiInterfaces::setSelectedItem);

        topForm.add(midiInterfaces);
        topForm.add(audioInterfaces);

        JPanel bottomPanel = new JPanel();
        JButton playButton = new JButton("Play");
        bottomPanel.add(playButton);
        JButton cancelButton = new JButton("Cancel");
        bottomPanel.add(cancelButton);
        FileDropPanel fileDropPanel = new FileDropPanel();

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(topForm, BorderLayout.NORTH);
        panel.add(fileDropPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        HelixAudioClipLooper helixAudioClipLooper = new HelixAudioClipLooper((Exception e) ->
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(panel, "Error: " + e);
        });

        JFrame frame = new JFrame();
        frame.setContentPane(panel);
        frame.setResizable(false);
        frame.setSize(300,300);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("Helix Audio Clip Looper");
        frame.setVisible(true);
        playButton.addActionListener(e -> helixAudioClipLooper.play(fileDropPanel.getFile(), (MidiDevice.Info)midiInterfaces.getSelectedItem(), (Mixer.Info)audioInterfaces.getSelectedItem()));
        cancelButton.addActionListener(e -> helixAudioClipLooper.cancel());
    }

    private static List<Mixer.Info> getAudioInterfaces() {
        return Arrays.stream(AudioSystem.getMixerInfo()).filter(HelixAudioClipLooper::isOutputDevice).collect(Collectors.toList());
    }

    private static List<MidiDevice.Info> getMidiInterfaces() {
        return Arrays.stream(MidiSystem.getMidiDeviceInfo()).filter(HelixAudioClipLooper::isOutputDevice).collect(Collectors.toList());
    }

    private void cancel() {
        if (audioClipLooper != null) {
            audioClipLooper.cancel();
        }
    }

    private void play(File resource, MidiDevice.Info midiDeviceInfo, Mixer.Info mixerInfo) {
        if (resource == null || midiDeviceInfo == null || mixerInfo == null)
            return;
        if (audioClipLooper != null) {
            audioClipLooper.close();
            audioClipLooper = null;
        }
        try {
            MidiDevice midiDevice = MidiSystem.getMidiDevice(midiDeviceInfo);
            midiDevice.open();
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(resource);
            SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(audioInputStream.getFormat(), mixerInfo);
            sourceDataLine.open();

            Receiver receiver = midiDevice.getReceiver();
            this.audioClipLooper = new AudioClipLooper(sourceDataLine, receiver, errorHandler);
            audioClipLooper.setupLoop(audioInputStream);
        }
        catch (Exception e)
        {
            errorHandler.accept(e);
        }
    }

    private static boolean isOutputDevice(MidiDevice.Info info) {
        try {
            MidiDevice midiDevice = MidiSystem.getMidiDevice(info);
            return midiDevice.getMaxReceivers() != 0;
        }
        catch (Exception e) {
            return false;
        }
    }

    private static boolean isOutputDevice(Mixer.Info info) {
        try {
            Mixer mixer = AudioSystem.getMixer(info);
            Line.Info[] sourceLineInfo = mixer.getSourceLineInfo();
            return sourceLineInfo.length != 0;
        }
        catch (Exception e) {
            return false;
        }
    }

}
