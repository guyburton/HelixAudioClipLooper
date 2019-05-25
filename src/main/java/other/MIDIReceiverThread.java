package other;

import javax.sound.midi.*;

import static javax.sound.midi.ShortMessage.*;

public class MIDIReceiverThread extends Thread {

    private final MidiDevice input;

    public MIDIReceiverThread(MidiDevice input) throws MidiUnavailableException {
        this.input = input;
        input.open();
    }


    public static void main(String[] args) {
        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            try {

                MidiDevice midiDevice = MidiSystem.getMidiDevice(info);
                if (midiDevice.getMaxTransmitters() == 0)
                    continue;
                midiDevice.open();
                Transmitter transmitter = midiDevice.getTransmitter();
                if (info.getName().equals("Circuit")) {
                    System.out.println("Listening to " + info.getName() + " " + info.getDescription() + " " + info.getVendor());
                    transmitter.setReceiver(new MyReceiver(info));
                }
//                midiDevice.close();
            } catch (MidiUnavailableException e) {
                System.out.println("Ignoring " + info.getName() + " " + info.getDescription() + " " + e.getMessage());
            }
        }
        while(true)
            Thread.yield();

    }


    private static class MyReceiver implements Receiver {
        private final MidiDevice.Info info;
        private int clockCounter;

        public MyReceiver(MidiDevice.Info info) {
            this.info = info;
        }

        @Override
        public void send(MidiMessage message, long timeStamp) {
            StringBuilder builder = new StringBuilder();
            for (byte b : message.getMessage()) {
                builder.append(String.format("0x%x ", b));
            }

            String output = null;
            switch (message.getStatus() & 0xF0) {
                case NOTE_OFF:
                    output = String.format("Note [%d] [OFF] Channel [%d]", sm(message).getData1(), sm(message).getChannel());
                    break;
                case NOTE_ON:
                    output = String.format("Note [%d] [ON] Channel [%d]", sm(message).getData1(), sm(message).getChannel());
                    break;
                case CONTROL_CHANGE:
                    output = String.format("CC [%d] [%d] Channel [%d]", sm(message).getData1(), sm(message).getData2(), sm(message).getChannel());
                    break;
                case PROGRAM_CHANGE:
                    output = String.format("Program Change [%d]", sm(message).getData1());
                    break;
            }

            if (output == null)
            {
                switch(message.getStatus()) {
                    case ShortMessage.TIMING_CLOCK:
                        clockCounter++;
                        clockCounter %= 24;
                        if (clockCounter == 0) {
                            output = "Quarter note pulse";
                            break;
                        }
                        else {
                            return; // dont print anything!
                        }
                    case ShortMessage.START:
                        output = "START SEQUENCE";
                        break;
                    case ShortMessage.STOP:
                        output = "STOP SEQUENCE";
                        break;
                    default:
                        output = String.format("Status: [%x] Message: [%s]", message.getStatus(), builder.toString());
                }
            }
            System.out.println(String.format("Device [%s] Timestamp [%d] %s", info.getName(), timeStamp, output));
        }

        private ShortMessage sm(MidiMessage message) {
            return (ShortMessage) message;
        }

        @Override
        public void close() {

        }
    }
}
