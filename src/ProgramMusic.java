import javax.sound.midi.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.Channel;
import java.util.ArrayList;

public class ProgramMusic {

    public static void main(String[] args) {

        int channel = 0;
        int durationMultiplier = 1;
        int reverbTimeMilli = 5000;


        String filepath = "./hello.txt";
        File file = new File(filepath);
        filepath = "./hello.zip";


        play(file, channel, durationMultiplier, reverbTimeMilli);
        export(file, channel, durationMultiplier, reverbTimeMilli);
        file = new File(filepath);
        play(file, channel, durationMultiplier, reverbTimeMilli);
        export(file, channel, durationMultiplier, reverbTimeMilli);

    }

    public static void export(File file, int channel, int durationMultiplier, int reverbTimeMilli) {
        try {
            Sequence sequence = new Sequence(Sequence.PPQ, 200);
            Track track1 = sequence.createTrack();

            ShortMessage sm = new ShortMessage();
            sm.setMessage(ShortMessage.PROGRAM_CHANGE, 0, channel, 0);
            track1.add(new MidiEvent(sm, 0));

            FileInputStream inputStream = new FileInputStream(file);
            byte[] bytes = inputStream.readAllBytes();
            inputStream = new FileInputStream(file);
            int numNotes = inputStream.available() / 3;
            System.out.println("Total Notes to Write: " + numNotes);
            long tick = 0;
            for (int i = 0; i < numNotes; i++) {
                int note = Math.abs(inputStream.read()) % 127;
                int velocity = Math.abs(inputStream.read()) % 127;
                int duration = (Math.abs(inputStream.read()) % 127) * durationMultiplier;

                double progress = ((double) i / (double) (numNotes - 1)) * 100.0;

                ShortMessage on = new ShortMessage();
                on.setMessage(ShortMessage.NOTE_ON, channel, note, velocity);
                ShortMessage off = new ShortMessage();
                off.setMessage(ShortMessage.NOTE_OFF, channel, note, velocity);

                MidiEvent onEvent = new MidiEvent(on, tick);
                tick += duration;
                MidiEvent offEvent = new MidiEvent(off, tick);

                track1.add(onEvent);
                track1.add(offEvent);

                System.out.println(
                        "Write Progress: " + progress + "%" +
                                ", Note: " + note +
                                ", Velocity: " + velocity +
                                ", Duration: " + duration + "ms"
                );
            }

            // Adds a note at the end to serve as the end of the file.
            tick += reverbTimeMilli;
            ShortMessage end = new ShortMessage();
            end.setMessage(ShortMessage.NOTE_OFF, channel, 0, 0);
            MidiEvent endEvent = new MidiEvent(end, tick);
            track1.add(endEvent);

            int[] allowedTypes = MidiSystem.getMidiFileTypes(sequence);
            MidiSystem.write(sequence, allowedTypes[0], new File(file.getName() + ".mid"));

        } catch (InvalidMidiDataException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void play(File file, int channel, int durationMultiplier, int reverbTimeMilli) { //, Synthesizer synthesizer, int channel, int durationMultiplier) {
        try {

            Synthesizer synthesizer = getSynthesizer(channel);
            MidiChannel[] channels = synthesizer.getChannels();
            synthesizer.open();

            FileInputStream inputStream = new FileInputStream(file);

            int numNotes = (inputStream.available() / 3);
            System.out.println("Total Notes: " + numNotes);
            for (int i = 0; i < numNotes; i++) {
                int note = Math.abs(inputStream.read()) % 127;
                int velocity = Math.abs(inputStream.read()) % 127;
                int duration = (Math.abs(inputStream.read()) % 127) * durationMultiplier;

                double progress = ((double) i/ (double) (numNotes - 1)) * 100;

                System.out.println(
                        "Progress: " + progress + "%" +
                        ", Note: " + note +
                        ", Velocity: " + velocity +
                        ", Duration: " + duration + "ms"
                );

                channels[channel].noteOn(note, velocity);
                Thread.sleep(duration);
            }

            Thread.sleep(reverbTimeMilli);
            synthesizer.close();
        } catch (MidiUnavailableException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public static Synthesizer getSynthesizer(int channel) {
        Synthesizer synthesizer = null;
        try {
            synthesizer = MidiSystem.getSynthesizer();
            Instrument[] instruments = synthesizer.getDefaultSoundbank().getInstruments();
            synthesizer.loadInstrument(instruments[channel]);
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    return synthesizer;
    }
}
