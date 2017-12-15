package org.mentionbattle.snadapter.impl.socialnetworks.handlers;
import twitter4j.TwitterStream;
import twitter4j.StatusListener;

class Twitter4jFixer {
    public static void addListener(TwitterStream stream, StatusListener listener) {
        stream.addListener(listener);
    }
}