package com.oddrock.caj2pdf.utils;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.oddrock.common.media.WavPlayer;

public class SoundNoticer implements Runnable{
	private int count;
	private String wavpath;
	public SoundNoticer(String wavpath, int count) {
		super();
		this.count = count;
		this.wavpath = wavpath;
	}
	public void run() {
		try {
			WavPlayer.play(wavpath, count);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
}
