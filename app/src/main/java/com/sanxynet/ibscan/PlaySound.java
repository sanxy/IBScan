package com.sanxynet.ibscan;

import java.util.Timer;
import java.util.TimerTask;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class PlaySound 
{

	private final int duration = 1; // seconds
	private final int sampleRate = 8000;
	private final int numSamples = this.duration * this.sampleRate;
	private final double sample[] = new double[this.numSamples];
	private final double freqOfTone = 880; // hz

	private final byte generatedSnd[] = new byte[2 * this.numSamples];

	public PlaySound() 
	{
		for (int i = 0; i < this.numSamples; ++i) 
		{
			this.sample[i] = Math.sin(2 * Math.PI * i / (this.sampleRate / this.freqOfTone));
		}

		// convert to 16 bit pcm sound array
		// assumes the sample buffer is normalised.
		int idx = 0;
		for (final double dVal : this.sample) 
		{
			// scale to maximum amplitude
			final short val = (short) ((dVal * 32767));
			// in 16 bit wav PCM, first byte is the low order byte
			this.generatedSnd[idx++] = (byte) (val & 0x00ff);
			this.generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

		}
	}

	public void playSound() 
	{
		final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
				this.sampleRate, AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT, this.numSamples,
				AudioTrack.MODE_STATIC);
		audioTrack.write(this.generatedSnd, 0, this.generatedSnd.length);
		audioTrack.play();
		//new added code, release audioTrack, solving exiting issue on Huawei after mutliple catpures
		//needs to delay for one second, otherwise the voice can not be played full before it finsihes
		TimerTask task = new TimerTask() {
			public void run() {
				// execute the task
				audioTrack.stop();
				audioTrack.release();
			}
		};

		Timer timer = new Timer();
		timer.schedule(task, 1000);
	}
}
