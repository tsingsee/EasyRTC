package org.easydarwin.push;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Process;
import android.util.Log;

import com.squareup.otto.Subscribe;

import org.easydarwin.android.aio.AudioIO;
import org.easydarwin.easyrtc.BuildConfig;
import org.easydarwin.muxer.EasyMuxer;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.easydarwin.easyrtc.TheApp.bus;

public class AudioStream {
    private final EasyMuxer muxer;
    private final AudioIO aio;
    private int samplingRate = 8000;
    private int bitRate = 8000;
    private int BUFFER_SIZE = 2048;
    // 2240
    ByteBuffer tmp = ByteBuffer.allocate(BUFFER_SIZE);
    int mSamplingRateIndex = 0;
    //    AudioRecord mAudioRecord;
    MediaCodec mMediaCodec;
    EasyPusher easyPusher;
    private Thread mThread = null;
    String TAG = "EasyPusher";
    //final String path = Environment.getExternalStorageDirectory() + "/123450001.aac";

    protected MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    protected ByteBuffer[] mBuffers = null;

    /**
     * There are 13 supported frequencies by ADTS.
     **/
    public static final int[] AUDIO_SAMPLING_RATES = {96000, // 0
            88200, // 1
            64000, // 2
            48000, // 3
            44100, // 4
            32000, // 5
            24000, // 6
            22050, // 7
            16000, // 8
            12000, // 9
            11025, // 10
            8000, // 11
            7350, // 12
            -1, // 13
            -1, // 14
            -1, // 15
    };

    public AudioStream(EasyPusher easyPusher, EasyMuxer muxer, AudioIO aio) {
        this.easyPusher = easyPusher;
        this.muxer = muxer;
        this.aio = aio;
        int i = 0;
        for (; i < AUDIO_SAMPLING_RATES.length; i++) {
            if (AUDIO_SAMPLING_RATES[i] == samplingRate) {
                mSamplingRateIndex = i;
                break;
            }
        }
    }

    /**
     * 编码
     */
    public void startRecord() {
        mThread = new Thread(new Runnable() {

            long stampUS = 0;
            @Subscribe
            public void onAudio(AudioIO.Nearend pcm) {
                if (tmp.remaining() == BUFFER_SIZE){
                    stampUS = pcm.timeUS;
                }
                if (tmp.hasRemaining()){
                    tmp.put(pcm.buffer, 0, Math.min(tmp.remaining(), pcm.buffer.length));
                }
                if (tmp.hasRemaining()) return;
                int bufferIndex = 0;
                do {
                    tmp.clear();
                    synchronized (this) {
                        if (mMediaCodec == null) return;
                        bufferIndex = mMediaCodec.dequeueInputBuffer(10000);
                        final ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
                        if (bufferIndex >= 0) {
                            inputBuffers[bufferIndex].clear();
                            inputBuffers[bufferIndex].put(tmp);
                            mMediaCodec.queueInputBuffer(bufferIndex, 0, BUFFER_SIZE, stampUS, 0);
                            Log.i(TAG, String.format("queueInputBuffer timeus:%d", stampUS));
                        }else
                        {
                            Log.w(TAG,String.format("dequeueInputBuffer ret:"+bufferIndex));
                        }
                    }
                }while (!Thread.currentThread().isInterrupted() && bufferIndex < 0 && mThread != null);
                tmp.clear();
            }


            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

                try {
                    int bufferSize = AudioRecord.getMinBufferSize(samplingRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
//                    mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, samplingRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
                    synchronized (this){
                        mMediaCodec = MediaCodec.createEncoderByType("audio/mp4a-latm");
                        MediaFormat format = new MediaFormat();
                        format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
                        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
                        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
                        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, samplingRate);
                        format.setInteger(MediaFormat.KEY_AAC_PROFILE,
                                MediaCodecInfo.CodecProfileLevel.AACObjectLC);
                        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, BUFFER_SIZE);
                        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                        mMediaCodec.start();
                    }
                    mBuffers = mMediaCodec.getOutputBuffers();
                    aio.start();
                    bus.register(this);

                    ByteBuffer mBuffer = ByteBuffer.allocate(10240);
                    while (mThread != null) {
                        int index = 0;
                        do {
                            index = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 10000);
                            if (index >= 0) {

                                Log.i(TAG, String.format("dequeueOutputBuffer timeus:%d", mBufferInfo.presentationTimeUs));
                                if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                                    continue;
                                }
                                mBuffer.clear();
                                ByteBuffer outputBuffer = mBuffers[index];
                                if (muxer != null)
                                    muxer.pumpStream(outputBuffer, mBufferInfo, false);
                                outputBuffer.get(mBuffer.array(), 7, mBufferInfo.size);
                                outputBuffer.clear();
                                mBuffer.position(7 + mBufferInfo.size);
                                addADTStoPacket(mBuffer.array(), mBufferInfo.size + 7);
                                mBuffer.flip();
                                easyPusher.push(mBuffer.array(), 0, mBufferInfo.size + 7, mBufferInfo.presentationTimeUs / 1000, 0);
                                if (BuildConfig.DEBUG)
                                    Log.i(TAG, String.format("push audio stamp:%d", mBufferInfo.presentationTimeUs / 1000));
                                mMediaCodec.releaseOutputBuffer(index, false);
                                break;
                            } else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                                mBuffers = mMediaCodec.getOutputBuffers();
                            } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                                Log.v(TAG, "output format changed...");
                                MediaFormat newFormat = mMediaCodec.getOutputFormat();
                                if (muxer != null)
                                    muxer.addTrack(newFormat, false);
                            } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                                Log.v(TAG, "No buffer available...");
                            } else {
                                Log.e(TAG, "Message: " + index);
                            }
                        } while (mThread != null && index >= 0);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Record___Error!!!!!");
                    e.printStackTrace();
                } finally {
                    bus.unregister(this);
                    synchronized (this) {
                        if (mMediaCodec != null) {
                            mMediaCodec.release();
                            mMediaCodec = null;
                        }
                    }
                    try {
                        aio.release();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "AudioProducer");
        mThread.start();
    }

    private void addADTStoPacket(byte[] packet, int packetLen) {
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF1;
        packet[2] = (byte) (((2 - 1) << 6) + (mSamplingRateIndex << 2) + (1 >> 2));
        packet[3] = (byte) (((1 & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    public void stop() {
        try {
            Thread t = mThread;
            mThread = null;
            if (t != null) {
                t.interrupt();
                t.join();
            }
        } catch (InterruptedException e) {
            e.fillInStackTrace();
        }
    }

}
