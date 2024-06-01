/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Crypto Morin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.cryptomorin.xseries;

import com.google.common.base.Strings;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * <b>NoteBlockMusic</b> - Write music scripts for Minecraft.<br>
 * You can write small text scripts for Minecraft <a href="https://minecraft.wiki/w/Note_Block">note blocks</a>
 * without needing to use any redstone or building to make your music.
 * This class is independent of XSound.
 *
 * @author Crypto Morin
 * @version 3.0.0
 * @see Instrument
 * @see Note
 */
public final class NoteBlockMusic {
    /**
     * A list of shortcuts for instruments.
     * Full names are also cached.
     *
     * @since 1.0.0
     */
    private static final Map<String, Instrument> INSTRUMENTS = new HashMap<>(50);

    private static final Map<Instrument, XSound> INSTRUMENT_TO_SOUND = new EnumMap<>(Instrument.class);

    static {
        INSTRUMENT_TO_SOUND.put(Instrument.PIANO, XSound.BLOCK_NOTE_BLOCK_HARP);
        INSTRUMENT_TO_SOUND.put(Instrument.BASS_DRUM, XSound.BLOCK_NOTE_BLOCK_BASEDRUM);
        INSTRUMENT_TO_SOUND.put(Instrument.SNARE_DRUM, XSound.BLOCK_NOTE_BLOCK_SNARE);
        INSTRUMENT_TO_SOUND.put(Instrument.STICKS, XSound.BLOCK_NOTE_BLOCK_HAT);
        INSTRUMENT_TO_SOUND.put(Instrument.BASS_GUITAR, XSound.BLOCK_NOTE_BLOCK_BASS);
        INSTRUMENT_TO_SOUND.put(Instrument.FLUTE, XSound.BLOCK_NOTE_BLOCK_FLUTE);
        INSTRUMENT_TO_SOUND.put(Instrument.BELL, XSound.BLOCK_NOTE_BLOCK_BELL);
        INSTRUMENT_TO_SOUND.put(Instrument.GUITAR, XSound.BLOCK_NOTE_BLOCK_GUITAR);
        INSTRUMENT_TO_SOUND.put(Instrument.CHIME, XSound.BLOCK_NOTE_BLOCK_CHIME);
        INSTRUMENT_TO_SOUND.put(Instrument.XYLOPHONE, XSound.BLOCK_NOTE_BLOCK_XYLOPHONE);
        INSTRUMENT_TO_SOUND.put(Instrument.IRON_XYLOPHONE, XSound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE);
        INSTRUMENT_TO_SOUND.put(Instrument.COW_BELL, XSound.BLOCK_NOTE_BLOCK_COW_BELL);
        INSTRUMENT_TO_SOUND.put(Instrument.DIDGERIDOO, XSound.BLOCK_NOTE_BLOCK_DIDGERIDOO);
        INSTRUMENT_TO_SOUND.put(Instrument.BIT, XSound.BLOCK_NOTE_BLOCK_BIT);
        INSTRUMENT_TO_SOUND.put(Instrument.BANJO, XSound.BLOCK_NOTE_BLOCK_BANJO);
        INSTRUMENT_TO_SOUND.put(Instrument.PLING, XSound.BLOCK_NOTE_BLOCK_PLING);
    }

    static {
        // Based on their XSound equivalent:
        INSTRUMENTS.put("HARP", Instrument.PIANO);
        INSTRUMENTS.put("BASEDRUM", Instrument.BASS_DRUM);
        INSTRUMENTS.put("BASE_DRUM", Instrument.BASS_DRUM);
        INSTRUMENTS.put("SNARE", Instrument.SNARE_DRUM);
        INSTRUMENTS.put("BASS", Instrument.BASS_GUITAR);
        INSTRUMENTS.put("COWBELL", Instrument.COW_BELL);

        // Add instrument shortcuts.
        for (Instrument instrument : Instrument.values()) {
            String name = instrument.name();
            INSTRUMENTS.put(name, instrument);

            StringBuilder alias = new StringBuilder(String.valueOf(name.charAt(0)));
            int index = name.indexOf('_');
            if (index != -1) alias.append(name.charAt(index + 1));

            if (INSTRUMENTS.putIfAbsent(alias.toString(), instrument) != null) {
                for (int i = 0; i < name.length(); i++) {
                    char ch = name.charAt(i);
                    if (ch == '_') {
                        i++;
                    } else {
                        alias.append(ch);
                        if (INSTRUMENTS.putIfAbsent(alias.toString(), instrument) == null) break;
                    }
                }
            }
        }
    }

    private NoteBlockMusic() {
    }

    @Nonnull
    public static XSound getSoundFromInstrument(@Nonnull Instrument instrument) {
        return INSTRUMENT_TO_SOUND.get(instrument);
    }

    /**
     * Gets a note tone from a character. Can't be optimized further using an array
     * since switch statement here can be optimized by JIT even more.
     * <p>
     * The character paseed to this method is assumed to be uppercase,
     * otherwise it needs to be {@code ch & 0x5f} manually.
     * <p>
     * https://minecraft.wiki/w/Note_Block#Notes
     *
     * @param ch the character of the note tone.
     * @return the note tone or null if not found.
     * @since 3.0.0
     */
    @Nullable
    public static Note.Tone getNoteTone(char ch) {
        switch (ch) {
            case 'A':
                return Note.Tone.A;
            case 'B':
                return Note.Tone.B;
            case 'C':
                return Note.Tone.C;
            case 'D':
                return Note.Tone.D;
            case 'E':
                return Note.Tone.E;
            case 'F':
                return Note.Tone.F;
            case 'G':
                return Note.Tone.G;
            default:
                return null;
        }
    }

    /**
     * A pre-written music script to test with {@link #playMusic(Player, Supplier, String)}
     * If you made a cool script using this let me know, I'll put it here.
     * You can still give me the script and I'll put it on the Spigot page.
     *
     * @param player the player to send the notes to.
     * @return the async task handling the notes.
     * @since 1.0.0
     */
    public static CompletableFuture<Void> testMusic(@Nonnull Player player) {
        return playMusic(player, player::getLocation, // Starting piece of Megalovania (not perfectly toned, it's screwed up)
                "PIANO,D,2,100 PIANO,B#1 200 PIANO,F 250 PIANO,E 250 PIANO,B 200 PIANO,A 100 PIANO,B 100 PIANO,E");
    }

    /**
     * Plays a music from a file.
     * This file can have YAML comments (#) and empty lines.
     *
     * @param player   the player to play the music to.
     * @param location the location to play the notes to.
     * @param path     the path of the file to read the music notes from.
     * @return the async task handling the file operations and music parsers.
     * @see #playMusic(Player, Supplier, String)
     * @since 1.0.0
     */
    public static CompletableFuture<Void> fromFile(@Nonnull Player player, @Nonnull Supplier<Location> location, @Nonnull Path path) {
        return CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    parseInstructions(line).play(player, location, true);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    /**
     * This is a very special and unique method.
     * This method allows you to write your own Minecraft music without needing to use
     * redstones and note blocks.
     * <p>
     * We'll take a whole thread for the music for blocking requests.
     * <b>Format:</b><p>
     * Instrument, Tone, Repeat (optional), Repeating Delay (optional, required if Repeat is used) [Next Delay]<br>
     * Both delays are in milliseconds.<br>
     * Also you can use segments (segment) to repeat a segment multiple times.
     * <p>
     * <b>Example</b><p>
     * Shortcuts:
     * {@code BD,G 20 BD,G 20 BD,G -> BD,G,3,20}
     * <pre>
     * (BD,G,3,20 BG,E,5,10),1000,2 1000 BA,A
     *
     * Translated:
     * Play BASS_DRUM with tone G 3 times every 20 ticks.<br>
     * Play BASS_GUITAR with tone E 5 times every 10 ticks.<br>
     * Play those ^ again two times with 1 second delay between repeats.<br>
     * Wait 1000ms.<br>
     * Play BANJO with tone A once.
     * </pre>
     * <p>
     * <b>Note Tones</b><p>
     * Available Note Tones: G, A, B, C, D, E, F (Idk why G is the first one) {@link org.bukkit.Note.Tone}<br>
     * You can also use sharp or flat tones by using '#' for sharp and '_' for flat e.g. B_ C#<br>
     * Octave numbers 1 and 2 can be used.
     * C1, C#1, B_1, D_2
     * <p>
     * <b>Instruments</b><p>
     * Available Instruments: Basically the first letter of every instrument. E.g. {@code BD -> BASS_DRUM}<br>
     * You can also use their full name. {@link Instrument}
     * <p>
     * <b>CompletableFuture</b><p>
     * Warning: Do not use blocking methods such as join() or get()
     * You may use cancel() or the then... methods.
     *
     * @param player   in order to play the note we need a player instance. Any player.
     * @param location the location to play this note to.
     * @param script   the music script.
     * @return the async task processing the script.
     * @see #fromFile(Player, Supplier, Path)
     * @since 1.0.0
     */
    public static CompletableFuture<Void> playMusic(@Nonnull Player player, @Nonnull Supplier<Location> location, @Nullable String script) {
        // We don't want to mess around in the main thread.
        // Sounds are thread-safe.
        return CompletableFuture.runAsync(() -> {
            if (Strings.isNullOrEmpty(script)) return;
            Sequence seq = parseInstructions(script);
            seq.play(player, location, true);
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    public static Sequence parseInstructions(@Nonnull CharSequence script) {
        return new InstructionBuilder(script).sequence;
    }

    /**
     * Method used to handle delays of instructions.
     * This method should always be called in another thread to
     * avoid freezing the main Minecraft thread.
     *
     * @param fermata (delay) in milliseconds.
     */
    private static void sleep(long fermata) {
        try {
            Thread.sleep(fermata);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Parses a Minecraft {@link Note} with its {@link org.bukkit.Note.Tone}
     * With the format: {@literal <tone>[pitch][octave]}
     * <p>
     * Available Note Tones: G, A, B, C, D, E, F (Idk why G is the first one) {@link org.bukkit.Note.Tone}<br>
     * You can also use sharp or flat tones by using '#' for sharp and '_' for flat e.g. B_ C#<br>
     * Octave numbers 1 and 2 can be used.
     * C1, C#1, B_1, D_2
     * <p>
     * <a href="https://en.wikipedia.org/wiki/Musical_tone">Tones</a>:<br>
     *     <ul>
     *         <li><a href="https://en.wikipedia.org/wiki/Sharp_(music)">Sharp Notes</a></li>
     *         <li><a href="https://en.wikipedia.org/wiki/Flat_(music)">Flat Notes</a></li>
     *         <li><a href="https://en.wikipedia.org/wiki/Natural_(music)">Natural Notes</a></li>
     *     </ul>
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Pitch_(music)">Pitch</a>
     * <a href="https://en.wikipedia.org/wiki/Octave">Octave</a>
     *
     * @return a note with a tone.
     * @since 3.0.0
     */
    @Nullable
    public static Note parseNote(@Nonnull String note) {
        Note.Tone tone = getNoteTone((char) (note.charAt(0) & 0x5f)); // Doesn't matter if it's already uppercase.
        if (tone == null) return null;

        int len = note.length();
        char toneType = ' ';
        int octave = 0;

        if (len > 1) {
            toneType = note.charAt(1);
            if (isDigit(toneType)) octave = toneType - '0'; // parseInt for single char
            else if (len > 2) {
                char octaveDigit = note.charAt(2);
                if (isDigit(octaveDigit)) octave = octaveDigit - '0';
            }

            if (octave < 0 || octave > 2) octave = 0;
        }

        return toneType == '#' ?
                Note.sharp(octave, tone) : toneType == '_' ?
                Note.flat(octave, tone) :
                Note.natural(octave, tone);
    }

    /**
     * {@link Character#isDigit(char)} won't work perfectly in this case.
     *
     * @param ch the character to check.
     * @return if and only if this character is an English digit number.
     * @since 1.2.0
     */
    private static boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    @SuppressWarnings("deprecation")
    public static float noteToPitch(@Nonnull Note note) {
        return (float) Math.pow(2.0D, ((double) note.getId() - 12.0D) / 12.0D);
    }

    private enum InstructionParserPhase {
        NEUTRAL {
            @Override
            protected InstructionParserPhase next() {
                return INSTRUMENT;
            }

            @Override
            protected char checkup(char ch) {
                throw new AssertionError("Checkup should not be performed on NEUTRAL instruction parser phase");
            }
        }, INSTRUMENT {
            @Override
            protected InstructionParserPhase next() {
                return NOTE;
            }

            @Override
            protected char checkup(char ch) {
                if (ch >= 'a' && ch <= 'z') return (char) (ch & 0x5f);
                return (ch >= 'A' && ch <= 'Z') || ch == '_' || ch == '-' ? ch : '\0';
            }
        }, NOTE {
            @Override
            protected InstructionParserPhase next() {
                return RESTATEMENT;
            }

            @Override
            protected char checkup(char ch) {
                if (ch >= 'a' && ch <= 'z') return (char) (ch & 0x5f);
                return (ch >= 'A' && ch <= 'Z') || isDigit(ch) || ch == '.' || ch == '_' || ch == '#' ? ch : '\0';
            }
        }, END_SEQ {
            @Override
            protected InstructionParserPhase next() {
                return RESTATEMENT;
            }

            @Override
            protected char checkup(char ch) {
                return 0;
            }
        }, RESTATEMENT {
            @Override
            protected InstructionParserPhase next() {
                return RESTATEMENT_DELAY;
            }

            @Override
            protected char checkup(char ch) {
                return isDigit(ch) ? ch : '\0';
            }
        }, RESTATEMENT_DELAY {
            @Override
            protected InstructionParserPhase next() {
                return FERMATA;
            }

            @Override
            protected char checkup(char ch) {
                return isDigit(ch) ? ch : '\0';
            }
        }, FERMATA {
            @Override
            protected InstructionParserPhase next() {
                return NEUTRAL;
            }

            @Override
            protected char checkup(char ch) {
                return isDigit(ch) ? ch : '\0';
            }
        };

        protected abstract InstructionParserPhase next();

        protected abstract char checkup(char ch);
    }

    @SuppressWarnings("StringBufferField")
    private static final class InstructionBuilder {
        @Nonnull final CharSequence script;
        final int len;
        final StringBuilder
                instrumentBuilder = new StringBuilder(10),
                pitchBuiler = new StringBuilder(3), volumeBuilder = new StringBuilder(3),
                restatementBuilder = new StringBuilder(10), restatementDelayBuilder = new StringBuilder(10),
                fermataBuilder = new StringBuilder(10);
        int i;
        boolean isSequence, isBuilding;
        Sequence sequence = new Sequence();
        InstructionParserPhase phase = InstructionParserPhase.NEUTRAL;
        StringBuilder currentBuilder;


        public InstructionBuilder(@Nonnull CharSequence script) {
            this.script = script;
            len = script.length();

            for (; i < len; i++) {
                char ch = script.charAt(i);
                switch (ch) {
                    case '(':
                        Sequence parent = new Sequence();
                        parent.parent = sequence;
                        sequence = parent;
                        break;
                    case ')':
                        if (sequence.parent == null) err("Cannot find start of the sequence for sequence at: " + i);
                        buildAndAddInstruction();
                        sequence = sequence.parent;
                        prepareHandlers();
                        phase = InstructionParserPhase.END_SEQ;
                        isSequence = true;
                        break;
                    case ' ':
                        if (!isBuilding) continue;
                        isBuilding = false;
                        switch (phase) {
                            case FERMATA:
                                buildAndAddInstruction();
                                prepareHandlers();
                                break;
                            case NOTE:
                            case RESTATEMENT_DELAY:
                                phase = InstructionParserPhase.FERMATA;
                                currentBuilder = fermataBuilder;
                                break;
                        }
                        break;
                    case ':': // Pitch/Note & Volume Separator
                        if (phase == InstructionParserPhase.NOTE) currentBuilder = volumeBuilder;
                        else err("Unexpected ':' pitch-volume separator at " + i + " with current phase: " + phase);
                        break;
                    case ',':
                        switch (phase) {
                            case INSTRUMENT:
                                currentBuilder = pitchBuiler;
                                break;
                            case NOTE:
                            case END_SEQ:
                                currentBuilder = restatementBuilder;
                                break;
                            case RESTATEMENT:
                                currentBuilder = restatementDelayBuilder;
                                break;
                            default:
                                err("Unexpected phase '" + phase + "' at index: " + i);
                        }
                        isBuilding = false;
                        phase = phase.next();
                        break;
                    default:
                        if (phase == InstructionParserPhase.NEUTRAL ||
                                (canBuildInstructionInPhase() && InstructionParserPhase.INSTRUMENT.checkup(ch) != '\0')) {
                            currentBuilder = instrumentBuilder;
                            if (phase == InstructionParserPhase.FERMATA) {
                                buildAndAddInstruction();
                                prepareHandlers();
                            }
                            phase = InstructionParserPhase.INSTRUMENT;
                        }
                        isBuilding = true;
                        if ((ch = phase.checkup(ch)) == '\0')
                            err("Unexpected char at index " + i + " with phase " + phase + ": " + script.charAt(i));
                        currentBuilder.append(ch);
                }
            }

//            if (!isBuilding) buildAndAddInstruction();
            buildAndAddInstruction();
            sequence = getRoot();
        }

        private Instruction buildInstruction() {
            int fermata = fermataBuilder.length() == 0 ? 0 : Integer.parseInt(fermataBuilder.toString());
            int restatement = restatementBuilder.length() == 0 ? 1 : Integer.parseInt(restatementBuilder.toString());
            int restatementFermata = restatementDelayBuilder.length() == 0 ? 0 : Integer.parseInt(restatementDelayBuilder.toString());
            // if (restatement > 1 && restatementFermata <= 0) throw new IllegalStateException("No restatement fermata found at " + i + " with restatement: " + restatement);

            Instruction instruction;
            if (isSequence) {
                instruction = new Sequence(restatement, restatementFermata, fermata);
            } else {
                String instrumentStr = instrumentBuilder.toString();
                XSound sound;
                Instrument instrument = INSTRUMENTS.get(instrumentStr);
                if (instrument == null) sound = XSound.matchXSound(instrumentStr).orElse(null);
                else sound = getSoundFromInstrument(instrument);

                String pitchStr = pitchBuiler.toString();
                float pitch;
                Note note = parseNote(pitchStr);
                if (note == null) pitch = Float.parseFloat(pitchStr);
                else pitch = noteToPitch(note);

                float volume = 5.0f;
                if (volumeBuilder.length() != 0) volume = Float.parseFloat(volumeBuilder.toString());

                instruction = new Sound(sound, pitch, volume, restatement, restatementFermata, fermata);
            }

            return instruction;
        }

        private void prepareHandlers() {
            instrumentBuilder.setLength(0);
            pitchBuiler.setLength(0);
            volumeBuilder.setLength(0);
            restatementBuilder.setLength(0);
            restatementDelayBuilder.setLength(0);
            fermataBuilder.setLength(0);

            phase = InstructionParserPhase.NEUTRAL;
            isBuilding = false;
            isSequence = false;
        }

        private boolean canBuildInstructionInPhase() {
            switch (phase) {
                case RESTATEMENT:
                case RESTATEMENT_DELAY:
                case FERMATA:
                    return true;
                default:
                    return false;
            }
        }

        private void buildAndAddInstruction() {
//            Sequence previous = sequence.parent == null ? sequence : sequence.parent;
            sequence.addInstruction(buildInstruction());
        }

        private Sequence getRoot() {
            Sequence sequence = this.sequence;
            while (sequence.parent != null) sequence = sequence.parent;
            return sequence;
        }

        private String illustrateError() {
            return '\n' + script.toString() + '\n' + Strings.repeat(" ", i) + '^';
        }

        private void err(String str) {
            throw new IllegalStateException(str + illustrateError());
        }
    }

    /**
     * An instruction that produces a sonud which consists of a {@link Instrument} and a {@link Note} with {@link org.bukkit.Note.Tone},
     * but without <a href="https://en.wikipedia.org/wiki/Duration_(music)">duration</a> or
     *
     * @since 3.0.0
     */
    public static class Sound extends Instruction {
        public XSound sound;
        /**
         * In Minecraft, you have no control over note
         * <a href="https://en.wikipedia.org/wiki/Duration_(music)">durations</a>.
         * A note, has a tone, and a tone is a named <a href="https://en.wikipedia.org/wiki/Pitch_(music)">pitch</a>
         * with a specific (constant) <a href="https://en.wikipedia.org/wiki/Timbre">timbre</a>.
         */
        public float volume, pitch;

        public Sound(Instrument instrument, Note note, float volume, int restatement, int restatementFermata, int fermata) {
            super(restatement, restatementFermata, fermata);
            this.sound = getSoundFromInstrument(instrument);
            this.pitch = noteToPitch(note);
            this.volume = volume;
        }

        public Sound(XSound sound, float pitch, float volume, int restatement, int restatementFermata, int fermata) {
            super(restatement, restatementFermata, fermata);
            this.sound = sound;
            this.pitch = pitch;
            this.volume = volume;
        }

        public void setSound(Instrument instrument) {
            this.sound = getSoundFromInstrument(instrument);
        }

        public void setPitch(Note note) {
            this.pitch = noteToPitch(note);
        }

        @Override
        public void play(Player player, Supplier<Location> location, boolean playAtLocation) {
            org.bukkit.Sound bukkitSound = sound.parseSound();
            for (int repeat = restatement; repeat > 0; repeat--) {
                Location finalLocation = location.get();
                if (bukkitSound != null) {
                    if (playAtLocation) {
                        finalLocation.getWorld().playSound(finalLocation, bukkitSound, volume, pitch);
                    } else {
                        player.playSound(finalLocation, bukkitSound, volume, pitch);
                    }
                }
                if (restatementFermata > 0) sleep(restatementFermata);
            }
            if (fermata > 0) sleep(fermata);
        }

        @Override
        public String toString() {
            return "Sound:{sound=" + sound + ", pitch=" + pitch + ", volume=" + volume +
                    ", restatement=" + restatement + ", restatementFermata=" + restatementFermata + ", fermata=" + fermata + '}';
        }
    }

    /**
     * Plays an instrument's notes in an ascending form.
     * This method is not really relevant to this utility class, but a nice feature.
     *
     * @param plugin      the plugin handling schedulers.
     * @param player      the player to play the note from.
     * @param playTo      the entity to play the note to.
     * @param instrument  the instrument.
     * @param ascendLevel the ascend level of notes. Can only be positive and not higher than 7
     * @param delay       the delay between each play.
     * @return the async task handling the operation.
     * @since 2.0.0
     */
    @Nonnull
    public static BukkitTask playAscendingNote(@Nonnull Plugin plugin, @Nonnull Player player, @Nonnull Entity playTo, @Nonnull Instrument instrument,
                                               int ascendLevel, int delay) {
        Objects.requireNonNull(player, "Cannot play note from null player");
        Objects.requireNonNull(playTo, "Cannot play note to null entity");

        if (ascendLevel <= 0) throw new IllegalArgumentException("Note ascend level cannot be lower than 1");
        if (ascendLevel > 7) throw new IllegalArgumentException("Note ascend level cannot be greater than 7");
        if (delay <= 0) throw new IllegalArgumentException("Delay ticks must be at least 1");

        return new BukkitRunnable() {
            int repeating = ascendLevel;

            @Override
            public void run() {
                player.playNote(playTo.getLocation(), instrument, Note.natural(1, Note.Tone.values()[ascendLevel - repeating]));
                if (repeating-- == 0) cancel();
            }
        }.runTaskTimerAsynchronously(plugin, 0, delay);
    }

    /**
     * An instruction is any musical <a href="https://en.wikipedia.org/wiki/Movement_(music)">movement</a> or
     * <a href="https://en.wikipedia.org/wiki/Section_(music)">section</a> that can be a <a href="">restatement</a>
     * and might have a <a href="https://en.wikipedia.org/wiki/Fermata">fermata</a>.
     * https://en.wikipedia.org/wiki/Repetition_(music)
     *
     * @since 3.0.0
     */
    public abstract static class Instruction {
        @Nullable
        public Sequence parent;
        public int restatement, restatementFermata, fermata;

        public Instruction(int restatement, int restatementFermata, int fermata) {
            this.restatement = restatement;
            this.restatementFermata = restatementFermata;
            this.fermata = fermata;
        }

        public abstract void play(Player player, Supplier<Location> location, boolean playAtLocation);

        public long getEstimatedLength() {
            return (long) restatement * restatementFermata;
        }
    }

    /**
     * A <a href="https://en.wikipedia.org/wiki/Sequence_(music)">sequence</a> is a restatement collection
     * of multiple other {@link Sequence}s and {@link Sound} that itself might be a part of another {@link Sequence}
     * <p>
     * A sequence in a script is shown with: {@code (instruction1, instruction2, ...),restatement,restatementFermata fermata}
     *
     * @since 3.0.0
     */
    public static class Sequence extends Instruction {
        public Collection<Instruction> instructions = new ArrayList<>(16);

        public Sequence() {
            super(1, 0, 0);
        }

        public Sequence(Instruction first) {
            super(1, 0, 0);
            instructions.add(first);
        }

        public Sequence(int restatement, int restatementFermata, int fermata) {
            super(restatement, restatementFermata, fermata);
        }

        @Override
        public void play(Player player, Supplier<Location> location, boolean playAtLocation) {
            for (int repeat = restatement; repeat > 0; repeat--) {
                for (Instruction instruction : instructions) {
                    instruction.play(player, location, playAtLocation);
                }
                if (restatementFermata > 0) sleep(restatementFermata);
            }
            if (fermata > 0) sleep(fermata);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(200 + (instructions.size() * 100));
            builder.append("Sequence:{restatement=").append(restatement).append(", restatementFermata=")
                    .append(restatementFermata).append(", fermata=").append(fermata).append(", instructions[");

            int i = 0, size = instructions.size();
            for (Instruction instruction : instructions) {
                builder.append(instruction);
                if (++i < size) builder.append(", ");
            }

            builder.append("]}");
            return builder.toString();
        }

        public void addInstruction(Instruction instruction) {
            instruction.parent = this;
            instructions.add(instruction);
        }

        @Override
        public long getEstimatedLength() {
            long result = (long) restatement * restatementFermata;
            for (Instruction instruction : instructions) result += instruction.getEstimatedLength();
            return result;
        }
    }
}
