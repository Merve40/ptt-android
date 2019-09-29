package com.yilmazgroup.ptt.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Utility class for handling streams.
 *
 * @author Merve Sahin
 */
public class StreamUtil {

    /**
     * Utility function for adding WAV headers to PCM chunk.
     *
     * @param data byte array containing PCM
     * @param channels (MONO=1, STEREO=2)
     * @param sampleRate (8000, 16000, 22050, .. 96000)
     *
     * @return byte array containing WAV headers and PCM data.
     */
    public static byte[] toWAV(byte[] data, short channels, int sampleRate, short bitDepth){

        int size = data.length + 44;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);

        byte[] littleBytes = ByteBuffer
                .allocate(14)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort(channels)
                .putInt(sampleRate)
                .putInt(sampleRate * channels * (bitDepth / 8))
                .putShort((short) (channels * (bitDepth / 8)))
                .putShort(bitDepth)
                .array();

        try {

            out.write(new byte[]{
                // RIFF header
                'R', 'I', 'F', 'F', // ChunkID
                0, 0, 0, 0, // ChunkSize (must be updated later)
                'W', 'A', 'V', 'E', // Format
                // fmt subchunk
                'f', 'm', 't', ' ', // Subchunk1ID
                16, 0, 0, 0, // Subchunk1Size
                1, 0, // AudioFormat
                littleBytes[0], littleBytes[1], // NumChannels
                littleBytes[2], littleBytes[3], littleBytes[4], littleBytes[5], // SampleRate
                littleBytes[6], littleBytes[7], littleBytes[8], littleBytes[9], // ByteRate
                littleBytes[10], littleBytes[11], // BlockAlign
                littleBytes[12], littleBytes[13], // BitsPerSample
                // data subchunk
                'd', 'a', 't', 'a', // Subchunk2ID
                0, 0, 0, 0, // Subchunk2Size (must be updated later)
            });

            out.write(data);
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] wavPcm = out.toByteArray();

        byte[] sizes = ByteBuffer
                .allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(size - 8) // ChunkSize
                .putInt(data.length) // Subchunk2Size
                .array();

        wavPcm[4] = sizes[0];
        wavPcm[5] = sizes[1];
        wavPcm[6] = sizes[2];
        wavPcm[7] = sizes[3];

        wavPcm[40] = sizes[4];
        wavPcm[41] = sizes[5];
        wavPcm[42] = sizes[6];
        wavPcm[43] = sizes[7];

        return wavPcm;
    }

    public static byte[] toWAV(short[] data, short channels, int sampleRate, short bitDepth){

        byte[] buffer = new byte[data.length*2];
        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(data);

        return toWAV(buffer, channels, sampleRate, bitDepth);
    }


}
