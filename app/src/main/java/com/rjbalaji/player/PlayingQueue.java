package com.rjbalaji.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rjbalaji.rjbalajipackage.AlbumSongDetailPackage;
import com.rjbalaji.util.Logger;
import com.rjbalaji.util.Utilities;

public class PlayingQueue {

	public static final int POSITION_NOT_AVAILABLE = -1;

	private List<AlbumSongDetailPackage> mQueue;
	private int mCurrentPosition;

	private static final int MIN_INDEX = 0;

	public PlayingQueue(List<AlbumSongDetailPackage> tracks, int currentPosition) {

		if (tracks != null && tracks.size() > 0) {

			mQueue = new ArrayList<AlbumSongDetailPackage>(tracks);
			mCurrentPosition = currentPosition;

		} else {

			mQueue = new ArrayList<AlbumSongDetailPackage>();
			mCurrentPosition = 0;
		}
	}

	public void updateCurrentPlayingPos(AlbumSongDetailPackage currentSong) {
		for (AlbumSongDetailPackage obj : mQueue) {
			if (currentSong.getMusic_id().equals(obj.getMusic_id())) {
				mCurrentPosition = mQueue.indexOf(obj);
				break;
			}
		}
	}

	public synchronized int size() {
		return mQueue.size();
	}

	public synchronized int getCurrentPosition() {
		return mCurrentPosition;
	}

	public synchronized AlbumSongDetailPackage getCurrentTrack() {
		if ((mQueue.size() - 1) >= mCurrentPosition) {
			return mQueue.get(mCurrentPosition);
		}
		return null;
	}

	public synchronized AlbumSongDetailPackage getNextTrack() {
		if ((mQueue.size() - 1) >= mCurrentPosition + 1) {
			return mQueue.get(mCurrentPosition + 1);
		}
		return null;
	}

	public synchronized AlbumSongDetailPackage getPreviousTrack() {
		if (MIN_INDEX <= mCurrentPosition - 1) {
			return mQueue.get(mCurrentPosition - 1);
		}
		return null;
	}

	public synchronized boolean hasNext() {
		if ((mQueue.size() - 1) >= mCurrentPosition + 1) {
			return true;
		}
		return false;
	}

	public synchronized boolean hasPrevious() {
		if (MIN_INDEX <= mCurrentPosition - 1) {
			return true;
		}
		return false;
	}

	public synchronized AlbumSongDetailPackage next() {
		if ((mQueue.size() - 1) >= mCurrentPosition + 1) {
			mCurrentPosition++;
			return mQueue.get(mCurrentPosition);

		} else {
			return null;
		}
	}

	public synchronized AlbumSongDetailPackage previous() {
		if (MIN_INDEX <= mCurrentPosition - 1) {
			mCurrentPosition--;
			return mQueue.get(mCurrentPosition);
		} else {
			return null;
		}
	}

	public synchronized void addNext(List<AlbumSongDetailPackage> tracks) {
		// if the current position is the last, just add it to the end of the
		// queue.
		try {
			if (mCurrentPosition == mQueue.size() - 1 || (mQueue.size() == 0)) {
				mQueue.addAll(tracks);
			} else {
				mQueue.addAll(mCurrentPosition + 1, tracks);
			}
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":108", e.toString());
		}
	}

	public synchronized void addToQueue(List<AlbumSongDetailPackage> tracks) {
		mQueue.addAll(tracks);
	}

	public synchronized AlbumSongDetailPackage goTo(int position) {
		// checks if the position in the Playinglist scope.
		if (MIN_INDEX <= position && ((mQueue.size() - 1) >= position)
				&& position != mCurrentPosition) {
			// sets the new position and retrieves the value of the new one.
			mCurrentPosition = position;
			return mQueue.get(position);
		}
		return null;
	}

	public synchronized AlbumSongDetailPackage goToNew(int position) {
		// checks if the position in the Playinglist scope.
		if (MIN_INDEX <= position && ((mQueue.size() - 1) >= position)) {
			// sets the new position and retrieves the value of the new one.
			mCurrentPosition = position;
			return mQueue.get(position);
		}
		return null;
	}

	public synchronized AlbumSongDetailPackage removeFrom(int position) {
		if (MIN_INDEX <= position && ((mQueue.size() - 1) >= position)) {
			if (position <= mCurrentPosition && position >= 0) {
				mCurrentPosition--;
			}
			if (mCurrentPosition == -1 && mQueue.size() > 1) {
				mCurrentPosition = 0;
			}
			return mQueue.remove(position);
		}
		return null;
	}
	public List<AlbumSongDetailPackage> getQueue() {
		return mQueue;
	}

	public void clearQueue() {
		mQueue.clear();
		mCurrentPosition = 0;
	}

	/**
	 * Retrieves a new deep copy of the list.
	 * 
	 * @return
	 */
	public List<AlbumSongDetailPackage> getCopy() {
		List<AlbumSongDetailPackage> tracks = new ArrayList<AlbumSongDetailPackage>();
		for (AlbumSongDetailPackage track : mQueue) {
			tracks.add(track.newCopy());
		}

		return tracks;
	}

	// ======================================================
	// For cached tracks when there is no connectivity.
	// ======================================================

	// public synchronized boolean hasNextCached() {
	// int index = mCurrentPosition + 1;
	// int maxIndex = mQueue.size() - 1;
	// if (maxIndex >= index) {
	// Track track = null;
	// for(; index <= maxIndex; index++) {
	// track = mQueue.get(index);
	// if (track.isCached()) {
	// return true;
	// }
	// }
	// }
	// return false;
	// }
	//
	// public synchronized boolean hasPreviousCached() {
	// int index = mCurrentPosition - 1;
	// if (MIN_INDEX <= index) {
	// Track track = null;
	// for(; index >= MIN_INDEX; index--) {
	// track = mQueue.get(index);
	// if (track.isCached()) {
	// return true;
	// }
	// }
	// }
	// return false;
	// }
	//
	// public synchronized Track nextCachedTrack() {
	// int index = mCurrentPosition + 1;
	// int maxIndex = mQueue.size() - 1;
	// if (maxIndex >= index) {
	// Track track = null;
	// for(; index <= maxIndex; index++) {
	// track = mQueue.get(index);
	// if (track.isCached()) {
	// mCurrentPosition = index;
	// return track;
	// }
	// }
	// }
	// return null;
	// }
	//
	// public synchronized Track previousCachedTrack() {
	// int index = mCurrentPosition - 1;
	// if (MIN_INDEX <= index) {
	// Track track = null;
	// for(; index >= MIN_INDEX; index--) {
	// track = mQueue.get(index);
	// if (track.isCached()) {
	// mCurrentPosition = index;
	// return track;
	// }
	// }
	// }
	// return null;
	// }

	/**
	 * Creates new shuffled playing queue from the given one, when the current
	 * playing Track will be the first an all the rest will be played after it
	 * but in a scrambled sequence.
	 */
	public static PlayingQueue createShuffledQueue(PlayingQueue playingQueue) {

		PlayingQueue scrambledPlayingQueue = null;

		// checks for end point scenarios.
		List<AlbumSongDetailPackage> tracks = playingQueue.getCopy();

		// if (Utils.isListEmpty(tracks)) {
		// throw new IllegalArgumentException("Given Plaing queue is empty!");
		// }

		try {

			if (!Utilities.isListEmpty(tracks)) {

				if (tracks.size() == 1) {
					// just return the queue to the nudnik.
					return playingQueue;
				}

				/*
				 * Does the following: 1. gets the current track and it's
				 * position (in case it appears couple of times in the queue).
				 * 2. aggregates all the tracks that are not the same one. 3.
				 * shuffles all the tracks. 4. adds the current one to be the
				 * first.
				 */
				AlbumSongDetailPackage currentTrack = playingQueue
						.getCurrentTrack();
				int currentTrackPosition = playingQueue.getCurrentPosition();

				List<AlbumSongDetailPackage> tracksToScramble = new ArrayList<AlbumSongDetailPackage>();

				int size = tracks.size();
				AlbumSongDetailPackage track = null;

				for (int i = 0; i < size; i++) {

					track = tracks.get(i);
					if (!track.getMusic_id().equals(currentTrack.getMusic_id())) {
						tracksToScramble.add(track.newCopy());
					}
				}

				// Everyday I'm shuffling.
				Collections.shuffle(tracksToScramble);

				// adds the current track to be the first.
				tracksToScramble.add(0, currentTrack.newCopy());

				scrambledPlayingQueue = new PlayingQueue(tracksToScramble,
						MIN_INDEX);

			} else {

				scrambledPlayingQueue = new PlayingQueue(tracks, MIN_INDEX);
			}

		} catch (IllegalArgumentException e) {

			new IllegalArgumentException("Given Plaing queue is empty!");
		}

		return scrambledPlayingQueue;
	}

	// xtpl
	public synchronized void setCurrentTrack(String trackId) {
		for (int i = 0; i < mQueue.size(); i++) {
			AlbumSongDetailPackage track = mQueue.get(i);
			if (track.getMusic_id().equals(trackId)) {
				mCurrentPosition = i;
				break;
			}
		}
	}

	public void updateTrack(AlbumSongDetailPackage track) {
		if (mQueue != null) {
			for (int i = 0; i < mQueue.size(); i++) {
				// System.out.println(mQueue.get(i).getId() +
				// " ::::::: updateTrack ::::::: " + track.getId());
				if (mQueue.get(i).getMusic_id().equals(track.getMusic_id())) {
					// System.out.println("Track found :::::::::::::: " + i);
					mQueue.set(i, track);
					break;
				}
			}
		}
	}

	public AlbumSongDetailPackage trackDragAndDrop(int from, int to) {
		if (mQueue != null) {
			if ((from < mCurrentPosition && to < mCurrentPosition)
					|| (from > mCurrentPosition && to > mCurrentPosition)) {
				final AlbumSongDetailPackage track = mQueue.get(from);
				mQueue.remove(from);
				mQueue.add(to, track);
			} else if (from == mCurrentPosition) {
				final AlbumSongDetailPackage track = mQueue.get(from);
				mQueue.remove(from);
				mQueue.add(to, track);
				mCurrentPosition = to;
			} else if (to == mCurrentPosition && from < to) {
				final AlbumSongDetailPackage track = mQueue.get(from);
				mQueue.remove(from);
				mQueue.add(to, track);
				mCurrentPosition--;
			} else if (to == mCurrentPosition && from > to) {
				final AlbumSongDetailPackage track = mQueue.get(from);
				mQueue.remove(from);
				mQueue.add(to, track);
				mCurrentPosition++;
			}
		}
		return null;
	}
	// xtpl
}
