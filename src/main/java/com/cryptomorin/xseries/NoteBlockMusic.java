/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Crypto Morin
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

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * <b>NoteBlockMusic</b> - Write music scripts for Minecraft.<br>
 * You can write small text scripts for Minecraft <a href="https://minecraft.gamepedia.com/Note_Block">note blocks</a>
 * without needing to use any redstone or building to make your music.
 * This class is independent of XSound.
 *
 * @author Crypto Morin
 * @version 2.0.0
 * @see Instrument
 * @see Note.Tone
 */
public class NoteBlockMusic {
    /**
     * A list of shortcuts for instruments.
     * Full names are also cached.
     *
     * @since 1.0.0
     */
    private static final Map<String, Instrument> INSTRUMENTS = new HashMap<>();

    static {
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

    /**
     * A pre-written music script to test with {@link #playMusic(Player, Location, String)}
     * If you made a cool script using this let me know, I'll put it here.
     * You can still give me the script and I'll put it on the Spigot page.
     *
     * @param player the player to send the notes to.
     * @return the async task handling the notes.
     * @since 1.0.0
     */
    public static CompletableFuture<Void> testMusic(@Nonnull Player player) {
        return playMusic(player, player.getLocation(), // Starting piece of Megalovania (not perfectly toned, it's screwed up)
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
     * @see #playMusic(Player, Location, String)
     * @since 1.0.0
     */
    public static CompletableFuture<Void> fromFile(@Nonnull Player player, @Nonnull Location location, @Nonnull Path path) {
        return CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    parseSegment(player, location, line, 1, 0);
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
     * @see #fromFile(Player, Location, Path)
     * @since 1.0.0
     */
    public static CompletableFuture<Void> playMusic(@Nonnull Player player, @Nonnull Location location, @Nullable String script) {
        // We don't want to mess around in the main thread.
        // Sounds are thread-safe.
        return CompletableFuture.runAsync(() -> {
            if (Strings.isNullOrEmpty(script)) return;
            parseSegment(player, location, script, 1, 0);
        });
    }

    /**
     * This method is used to play a segment multiple times.
     *
     * @since 1.0.0
     */
    private static void parseSegment(@Nonnull Player player, @Nonnull Location location, @Nonnull String script,
                                     int segmentRepeat, int segmentDelay) {
        ArrayList<String> repeater = new ArrayList<>();
        String[] splitScript = StringUtils.split(script, ' ');

        for (; segmentRepeat > 0; segmentRepeat--) {
            for (String action : splitScript) {
                int betweenDelay = NumberUtils.toInt(action);
                if (betweenDelay > 1) {
                    // Add to last Repeat Segment
                    if (!repeater.isEmpty()) {
                        String old = repeater.remove(repeater.size() - 1);
                        repeater.add(old + ' ' + betweenDelay);
                    }
                    try {
                        Thread.sleep(betweenDelay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                String[] split = StringUtils.split(StringUtils.deleteWhitespace(action.toUpperCase(Locale.ENGLISH)), ',');
                String instrumentStr = split[0].toUpperCase(Locale.ENGLISH);

                // Start of a new Repeat Segment
                if (instrumentStr.charAt(0) == '(') {
                    repeater.add(action.substring(1));
                    instrumentStr = instrumentStr.substring(1);
                } else {
                    int index = action.indexOf(')');

                    // Add to last Repeat Segment
                    if (!repeater.isEmpty()) {
                        String old = repeater.remove(repeater.size() - 1);
                        String add = index == -1 ? action : action.substring(0, index);
                        repeater.add(old + ' ' + add);
                    }

                    // End of last Repeat Segment
                    // Get the new segment ready for loop.
                    if (index != -1) {
                        String[] segmentProperties = StringUtils.split(action.substring(index + 1), ',');

                        int newSegmentRepeat = 1;
                        int newSegmentDelay = 0;

                        if (segmentProperties.length > 0) {
                            newSegmentRepeat = NumberUtils.toInt(segmentProperties[0]);
                            if (segmentProperties.length > 1) newSegmentDelay = NumberUtils.toInt(segmentProperties[1]);
                        }

                        parseSegment(player, location, repeater.remove(repeater.size() - 1), newSegmentRepeat, newSegmentDelay);
                        continue;
                    }
                }

                Instrument instrument = INSTRUMENTS.get(instrumentStr);
                if (instrument == null) continue;

                String note = split[1].toUpperCase(Locale.ENGLISH);
                Note.Tone tone = Enums.getIfPresent(Note.Tone.class, String.valueOf(note.charAt(0))).orNull();
                if (tone == null) continue;

                int len = note.length();
                char toneType = ' ';
                int octave = 0;

                if (len > 1) {
                    toneType = note.charAt(1);
                    if (isDigit(toneType)) octave = NumberUtils.toInt(String.valueOf(toneType));
                    else if (len > 2) octave = NumberUtils.toInt(String.valueOf(note.charAt(2)));

                    if (octave < 0 || octave > 2) octave = 0;
                }

                Note noteObj = toneType == '#' ?
                        Note.sharp(octave, tone) : toneType == '_' ?
                        Note.flat(octave, tone) :
                        Note.natural(octave, tone);

                int repeat = 1;
                int delay = 0;

                if (split.length > 2) {
                    repeat = NumberUtils.toInt(split[2]);
                    if (split.length > 3) delay = NumberUtils.toInt(split[3]);
                }

                for (; repeat > 0; repeat--) {
                    player.playNote(location, instrument, noteObj);

                    if (repeat != 0 && delay > 1) {
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }

            if (segmentDelay > 1) {
                try {
                    Thread.sleep(segmentDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
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
}
