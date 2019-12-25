/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Crypto Morin
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
import com.google.common.base.Enums;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/* References
 *
 * * * GitHub: https://github.com/CryptoMorin/XSeries/blob/master/NoteBlockMusic.java
 * * XSeries: https://www.spigotmc.org/threads/378136/
 * Note Blocks: https://minecraft.gamepedia.com/Note_Block
 */

/**
 * <b>NoteBlockMusic</b> - Write music scripts for Minecraft.<br>
 * Supports 1.8-1.15
 * This class is independent of XSound.
 *
 * @author Crypto Morin
 * @version 1.0.0
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
    private static final ImmutableMap<String, Instrument> INSTRUMENTS;
    /**
     * Not much efficient but we're still using this a lot.
     * Allows you to use spaces and new lines for really long scripts.
     *
     * @since 1.0.0
     */
    private static final Pattern SPLITTER = Pattern.compile("\\s+");

    static {
        // Add instrument shortcuts.
        Map<String, Instrument> instruments = new HashMap<>();
        for (Instrument instrument : Instrument.values()) {
            String name = instrument.name();
            instruments.put(name, instrument);

            StringBuilder alias = new StringBuilder(name.charAt(0) + "");
            int index = name.indexOf('_');
            if (index != -1) alias.append(name.charAt(index + 1));

            if (!instruments.containsKey(alias.toString())) instruments.put(alias.toString(), instrument);
            else {
                boolean start = false;
                for (char letter : name.toCharArray()) {
                    if (!start) {
                        start = true;
                        continue;
                    }
                    if (letter == '_') {
                        start = false;
                        continue;
                    }
                    alias.append(letter);
                    if (!instruments.containsKey(alias.toString())) {
                        instruments.put(alias.toString(), instrument);
                        break;
                    }
                }
            }
        }

        INSTRUMENTS = ImmutableMap.copyOf(instruments);
    }

    /**
     * A pre-written music script to test with {@link #playMusic(Player, Location, String)}
     * If you made a cool script using this let me know, I'll put it here.
     * You can still give me the script and I'll put it on the Spigot page.
     *
     * @since 1.0.0
     */
    public static CompletableFuture<Void> testMusic(Player player) {
        return playMusic(player, player.getLocation(), // Starting piece of Megalovania (not perfectly toned, it's screwed up)
                "PIANO,D,2,100 PIANO,B#1 200 PIANO,F 250 PIANO,E 250 PIANO,B 200 PIANO,A 100 PIANO,B 100 PIANO,E");
    }

    /**
     * Plays a music from a file.
     * This file can have YAML comments (#) and empty lines.
     *
     * @see #playMusic(Player, Location, String)
     * @since 1.0.0
     */
    public static CompletableFuture<Void> fromFile(Player player, Location location, Path path) {
        return CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    if (line.startsWith("#")) continue;

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
     * BD,G 20 BD,G 20 BD,G -> BD,G,3,20
     * <br>
     * (BD,G,3,20 BG,E,5,10),1000,2 1000 BA,A
     * ->
     * Play BASS_DRUM with tone G 3 times every 20 ticks.<br>
     * Play BASS_GUITAR with tone E 5 times every 10 ticks.<br>
     * Play those ^ again two times with 1 second delay between repeats.<br>
     * Wait 1000ms.<br>
     * Play BANJO with tone A once.
     * <p>
     * <b>Note Tones</b><p>
     * Available Note Tones: G, A, B, C, D, E, F (Idk why G is the first one) {@link org.bukkit.Note.Tone}<br>
     * You can also use sharp or flat tones by using '#' for sharp and '_' for flat e.g. B_ C#<br>
     * Octave numbers 1 and 2 can be used.
     * C1, C#1, B_1, D_2
     * <p>
     * <b>Instruments</b><p>
     * Available Instruments: Basically the first letter of every instrument. E.g. BD -> BASS_DRUM<br>
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
    public static CompletableFuture<Void> playMusic(Player player, Location location, String script) {
        // We don't want to mess around in the main thread.
        // Sounds are thread-safe.
        return CompletableFuture.runAsync(() -> parseSegment(player, location, script, 1, 0));
    }

    /**
     * This method is used to play a segment multiple times.
     *
     * @since 1.0.0
     */
    private static void parseSegment(Player player, Location location, String script,
                                     int segmentRepeat, int segmentDelay) {
        ArrayList<String> repeater = new ArrayList<>();
        String[] splitScript = SPLITTER.split(script);

        for (; segmentRepeat > 0; segmentRepeat--) {
            for (String action : splitScript) {
                int betweenDelay = NumberUtils.toInt(action);
                if (betweenDelay > 1) {
                    // Add to last Repeat Segment
                    if (!repeater.isEmpty()) {
                        String old = repeater.remove(repeater.size() - 1);
                        repeater.add(old + " " + betweenDelay);
                    }
                    try {
                        Thread.sleep(betweenDelay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                String[] split = StringUtils.split(StringUtils.deleteWhitespace(action.toUpperCase()), ',');
                String instrumentStr = split[0].toUpperCase();

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
                        repeater.add(old + " " + add);
                    }

                    // End of last Repeat Segment
                    // Get the new segment ready for loop.
                    if (index != -1) {
                        String[] segmentProperties = StringUtils.split(action.substring(index + 1), ',');

                        int newSegmentRepeat = 1;
                        int newSegmentDelay = 0;

                        if (segmentProperties.length > 0) {
                            try {
                                newSegmentRepeat = Integer.parseInt(segmentProperties[0]);
                                if (segmentProperties.length > 1)
                                    newSegmentDelay = Integer.parseInt(segmentProperties[1]);
                            } catch (NumberFormatException ignored) {
                            }
                        }

                        parseSegment(player, location, repeater.remove(repeater.size() - 1),
                                newSegmentRepeat, newSegmentDelay);
                        continue;
                    }
                }

                Instrument instrument = INSTRUMENTS.get(instrumentStr);
                if (instrument == null) continue;

                String note = split[1].toUpperCase();
                Note.Tone tone = Enums.getIfPresent(Note.Tone.class, note.charAt(0) + "").orNull();
                if (tone == null) continue;

                int len = note.length();
                char toneType = ' ';
                int octave = 0;

                if (len > 1) {
                    toneType = note.charAt(1);
                    if (Character.isDigit(toneType)) octave = NumberUtils.toInt(toneType + "");
                    else if (len > 2) octave = NumberUtils.toInt(note.charAt(2) + "");

                    if (octave < 0 || octave > 2) octave = 0;
                }

                Note noteObj = toneType == '#' ?
                        Note.sharp(octave, tone) : toneType == '_' ?
                        Note.flat(octave, tone) :
                        Note.natural(octave, tone);

                int repeat = 1;
                int delay = 0;

                if (split.length > 2) {
                    try {
                        repeat = Integer.parseInt(split[2]);
                        if (split.length > 3) delay = Integer.parseInt(split[3]);
                    } catch (NumberFormatException ignored) {
                    }
                }

                for (; repeat > 0; repeat--) {
                    player.playNote(location, instrument, noteObj);

                    if (repeat != 0 && delay > 1) {
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (segmentDelay > 1) {
                try {
                    Thread.sleep(segmentDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
